package roomescape.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.MemberErrorCode;
import roomescape.common.exception.code.ReservationErrorCode;
import roomescape.common.exception.code.ReservationTimeErrorCode;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.domain.Member;
import roomescape.domain.PaymentOrder;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.command.CreateReservationCommand;
import roomescape.dto.command.UpdateReservationCommand;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationWaitingOrderResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.PaymentOrderRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final ReservationWaitingService waitingService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationWaitingRepository reservationWaitingRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository,
                              PaymentOrderRepository paymentOrderRepository,
                              ReservationWaitingService waitingService) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.waitingService = waitingService;
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationCommand command, LocalDateTime now) {
        Member member = getMember(command.memberId());
        ReservationTime reservationTime = getTime(command.timeId());
        Theme theme = getTheme(command.themeId());
        ReservationSlot slot = new ReservationSlot(command.date(), reservationTime, theme);

        validateUniqueReservation(slot);
        validatePastDatetime(slot, now);

        Reservation reservation = Reservation.createWithoutId(member, slot);
        Reservation savedReservation = reservationRepository.save(reservation);

        String orderId = generateOrderId();
        paymentOrderRepository.save(PaymentOrder.createWithoutId(orderId, command.amount(), savedReservation));

        return ReservationResponse.from(savedReservation, orderId);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> getMyReservations(Long memberId) {
        List<MyReservationResponse> reservations = reservationRepository.findByMember_Id(memberId).stream()
                .map(MyReservationResponse::fromReservation)
                .toList();
        List<MyReservationResponse> reservationWaitings = reservationWaitingRepository.findByMember_IdOrderByCreatedAt(memberId)
                .stream()
                .map(waiting -> new ReservationWaitingOrderResponse(
                        waiting,
                        reservationWaitingRepository.countOrder(waiting.getSlot(), waiting.getId())
                ))
                .map(MyReservationResponse::fromReservationWaiting)
                .toList();

        return getMyReservationResponses(reservations, reservationWaitings);
    }

    @Transactional
    public ReservationResponse update(Long reservationId, UpdateReservationCommand command, LocalDateTime now) {
        Reservation reservation = getReservation(reservationId);
        ReservationTime time = getTime(command.timeId());
        ReservationSlot slot = new ReservationSlot(command.date(), time, reservation.getTheme());

        validateUniqueExcludingSelf(slot, reservationId);
        validatePastDatetime(slot, now);

        reservation.changeSlot(slot);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public void delete(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        paymentOrderRepository.findByReservation_Id(reservationId)
                .ifPresent(paymentOrderRepository::delete);
        reservationRepository.deleteById(reservationId);
        reservationRepository.flush();
        waitingService.promoteFirstWaiting(reservation.getSlot());
    }

    @Transactional
    public void cancelPendingByOrderId(String orderId) {
        if (orderId == null) {
            return;
        }
        paymentOrderRepository.findByOrderId(orderId).ifPresent(order -> {
            Reservation reservation = order.getReservation();
            paymentOrderRepository.delete(order);
            paymentOrderRepository.flush();
            reservationRepository.delete(reservation);
        });
    }

    private String generateOrderId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    private Member getMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomEscapeException(MemberErrorCode.NOT_FOUND));
    }

    private ReservationTime getTime(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeErrorCode.NOT_FOUND));
    }

    private Theme getTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.NOT_FOUND));
    }

    private void validateUniqueReservation(ReservationSlot slot) {
        if (reservationRepository.existsBySlot(slot)) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
    }

    private void validateUniqueExcludingSelf(ReservationSlot slot, long id) {
        if (reservationRepository.existsBySlotAndIdNot(slot, id)) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
    }

    private void validatePastDatetime(ReservationSlot slot, LocalDateTime now) {
        if (slot.isPast(now)) {
            throw new RoomEscapeException(ReservationErrorCode.PAST_DATETIME);
        }
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.NOT_FOUND));
    }

    private List<MyReservationResponse> getMyReservationResponses(List<MyReservationResponse> reservations,
                                                                  List<MyReservationResponse> reservationWaitings) {
        List<MyReservationResponse> result = new ArrayList<>();
        result.addAll(reservations);
        result.addAll(reservationWaitings);
        result.sort(
                Comparator.comparing(MyReservationResponse::date)
                        .thenComparing(r -> r.time().startAt())
                        .thenComparing(r -> r.theme().name())
        );
        return result;
    }
}
