package roomescape.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.policy.ReservationPolicy;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.exception.client.ResourceNotFoundException;
import roomescape.exception.server.DataInconsistencyException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.MyReservationResult;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationUpdateCommand;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationPolicy reservationPolicy;
    private final WaitingRepository waitingRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            ReservationPolicy reservationPolicy,
            WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationPolicy = reservationPolicy;
        this.waitingRepository = waitingRepository;
    }


    public List<ReservationResult> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResult::from)
                .toList();
    }

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

    public void deleteByOwner(Long id, String name) {
        Reservation reservation = findByIdAndName(id, name);
        reservationPolicy.validateCancellable(
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );

        reservationRepository.deleteById(id);
    }

    public ReservationResult updateByOwner(ReservationUpdateCommand command) {
        Reservation reservation = findByIdAndName(command.getId(), command.getName());
        reservationPolicy.validateUpdatable(
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );

        ReservationTime newTime = findTimeOrThrow(command.getTimeId());
        reservationPolicy.validateUpdateTarget(command.getDate(), newTime.getStartAt());
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
