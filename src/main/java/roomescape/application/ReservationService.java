package roomescape.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.command.ReservationCreateCommand;
import roomescape.application.dto.command.ReservationUpdateCommand;
import roomescape.application.dto.result.MyReservationResult;
import roomescape.application.dto.result.ReservationOrderResult;
import roomescape.application.dto.result.ReservationResult;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDateTime;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.Waitings;
import roomescape.domain.policy.ReservationPolicy;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.exception.client.ResourceNotFoundException;
import roomescape.exception.server.DataInconsistencyException;

@Service
public class ReservationService {
    // D2: 고정 금액. 테마별 가격은 이번 미션 범위 밖(이월).
    private static final long RESERVATION_AMOUNT = 1000L;


    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationPolicy reservationPolicy;
    private final WaitingRepository waitingRepository;
    private final PaymentService paymentService;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            ReservationPolicy reservationPolicy,
            WaitingRepository waitingRepository,
            PaymentService paymentService
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationPolicy = reservationPolicy;
        this.waitingRepository = waitingRepository;
        this.paymentService = paymentService;
    }


    public List<ReservationResult> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResult::from)
                .toList();
    }

    @Transactional
    public ReservationResult create(ReservationCreateCommand command) {
        ReservationTime time = findTimeOrThrow(command.getTimeId());
        Theme theme = findThemeOrThrow(command.getThemeId());

        validateNotDuplicated(command.getDate(), time.getId(), theme.getId());

        Reservation reservation = Reservation.create(
                command.getName(),
                command.getDate(),
                time,
                theme,
                reservationPolicy// 정책 객체가 과거 검증을 담당
        );

        Reservation saved = reservationRepository.save(reservation);
        return ReservationResult.from(saved);
    }


    /**
     * 사용자 예약 요청: '결제 대기' 예약 + 주문을 저장하고, 결제창에 필요한 정보를 돌려준다. 확정(CONFIRMED)은 successUrl 콜백의 PaymentService.confirm 에서
     * 이뤄진다.
     */
    @Transactional
    public ReservationOrderResult reserveWithPayment(ReservationCreateCommand command) {
        ReservationTime time = findTimeOrThrow(command.getTimeId());
        Theme theme = findThemeOrThrow(command.getThemeId());
        validateNotDuplicated(command.getDate(), time.getId(), theme.getId());

        Reservation reservation = Reservation.createPending(
                command.getName(), command.getDate(), time, theme, reservationPolicy);
        Reservation saved = reservationRepository.save(reservation);

        String orderId = paymentService.prepare(saved.getId(), RESERVATION_AMOUNT);

        return new ReservationOrderResult(saved.getId(), orderId, RESERVATION_AMOUNT, theme.getName());
    }


    @Transactional
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<MyReservationResult> findMyReservationsAndWaitings(String name) {
        List<MyReservationResult> results = new ArrayList<>();

        reservationRepository.findByNameOrderByDateAscTimeAsc(name).forEach(r ->
                results.add(MyReservationResult.ofReservation(
                        r.getId(), r.getDate(), r.getTime(), r.getTheme())));

        waitingRepository.findByName(name).forEach(w ->
                results.add(MyReservationResult.ofWaiting(
                        w.getId(), w.getDate(), w.getTime(), w.getTheme(), w.getOrderIndex())));

        results.sort(Comparator
                .comparing(MyReservationResult::getDate)
                .thenComparing(r -> r.getTime().getStartAt()));

        return results;
    }

    @Transactional
    public void deleteByOwner(Long id, String name) {
        Reservation reservation = findByIdAndName(id, name);
        reservationPolicy.validateCancellable(reservation.dateTime());

        reservationRepository.deleteById(id);
        promoteFirstWaitingIfExists(reservation);
    }

    private void promoteFirstWaitingIfExists(Reservation canceled) {
        Waitings waitings = new Waitings(waitingRepository.findBySlot(
                canceled.getDate(),
                canceled.getTime().getId(),
                canceled.getTheme().getId()
        ));

        Optional<Waiting> firstWaiting = waitings.firstWaiting();
        if (firstWaiting.isEmpty()) {
            return;
        }

        Waiting first = firstWaiting.get();
        reservationRepository.save(Reservation.promote(first));
        waitingRepository.deleteById(first.getId());
        reorderRemaining(waitings, first.getOrderIndex());
    }

    private void reorderRemaining(Waitings waitings, int removedOrder) {
        for (Waiting w : waitings.reorderAfterRemoval(removedOrder)) {
            waitingRepository.updateOrderIndex(w.getId(), w.getOrderIndex());
        }
    }

    @Transactional
    public ReservationResult updateByOwner(ReservationUpdateCommand command) {
        Reservation reservation = findByIdAndName(command.getId(), command.getName());
        reservationPolicy.validateUpdatable(reservation.dateTime());

        ReservationTime newTime = findTimeOrThrow(command.getTimeId());
        reservationPolicy.validateUpdateTarget(
                ReservationDateTime.of(command.getDate(), newTime.getStartAt()));
        validateNotDuplicatedExcludingSelf(command, reservation.getTheme().getId());

        reservationRepository.updateDateAndTime(command.getId(), command.getDate(), command.getTimeId());
        return ReservationResult.from(findUpdatedReservationOrThrow(command.getId()));
    }

    private ReservationTime findTimeOrThrow(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 시간입니다."));
    }

    private Theme findThemeOrThrow(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 테마입니다."));
    }

    private Reservation findUpdatedReservationOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new DataInconsistencyException(
                        "저장된 예약을 찾을 수 없습니다. 데이터 정합성 문제가 의심됩니다."
                ));
    }

    private Reservation findByIdAndName(Long id, String name) {
        return reservationRepository.findById(id)
                .filter(r -> r.getName().equals(name))
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약입니다."));
    }


    private void validateNotDuplicated(LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsByDateAndTimeAndTheme(date, timeId, themeId)) {
            throw new BusinessRuleViolationException(
                    "해당 시간은 이미 예약되었습니다. 다른 시간을 선택해 주세요."
            );
        }
    }

    private void validateNotDuplicatedExcludingSelf(ReservationUpdateCommand command, Long themeId) {
        if (reservationRepository.existsByDateAndTimeAndThemeExcludingId(
                command.getDate(), command.getTimeId(), themeId, command.getId())) {
            throw new BusinessRuleViolationException(
                    "해당 시간은 이미 예약되었습니다. 다른 시간을 선택해 주세요."
            );
        }
    }
}
