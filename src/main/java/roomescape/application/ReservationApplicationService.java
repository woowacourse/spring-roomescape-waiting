package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Payment;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Reservations;
import roomescape.domain.Theme;
import roomescape.domain.WaitingWithOrder;
import roomescape.dto.PaymentConfirmRequest;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.dto.ReservationWaitingRequest;
import roomescape.dto.TimeWithStatusResponse;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.ConflictException;
import roomescape.exception.NotFoundException;
import roomescape.service.PaymentService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ReservationWaitingService;
import roomescape.service.ThemeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationApplicationService {

    private static final String CANNOT_DELETE_TIME_IN_USE = "ID %d번 시간을 사용 중인 예약이 존재하여 시간을 삭제할 수 없습니다.";
    private static final String CANNOT_DELETE_THEME_IN_USE = "ID %d번 테마를 사용 중인 예약이 존재하여 테마를 삭제할 수 없습니다.";
    private static final String ALREADY_EXISTS_ADD_RESERVATION = "해당 날짜와 시간, 테마에 이미 예약이 존재합니다.";
    private static final String PAST_RESERVATION_REJECTED = "지난 시각에는 예약할 수 없습니다.";
    private static final String PAST_RESERVATION_UPDATE_REJECTED = "지난 시각으로 예약을 변경할 수 없습니다.";
    private static final String PAST_RESERVATION_WAITING_REJECTED = "지난 시각에는 대기할 수 없습니다.";
    private static final String EXPIRED_RESERVATION_UPDATE_REJECTED = "이미 지난 예약은 변경할 수 없습니다.";
    private static final String PAST_RESERVATION_CANCEL_REJECTED = "이미 지난 예약은 취소할 수 없습니다.";
    private static final String PAST_RESERVATION_DELETE_REJECTED = "지난 예약은 삭제할 수 없습니다.";
    private static final String ALREADY_WAITING = "이미 대기를 신청한 예약입니다.";
    private static final long PAYMENT_AMOUNT = 50_000L;

    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ReservationWaitingService reservationWaitingService;
    private final ThemeService themeService;
    private final PaymentService paymentService;

    public ReservationApplicationService(
            ReservationService reservationService,
            ReservationTimeService reservationTimeService,
            ReservationWaitingService reservationWaitingService,
            ThemeService themeService,
            PaymentService paymentService
    ) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.reservationWaitingService = reservationWaitingService;
        this.themeService = themeService;
        this.paymentService = paymentService;
    }

    @Transactional
    public void deleteTime(Long id) {
        if (reservationService.hasReservationsByTimeId(id)) {
            throw new BusinessRuleViolationException(String.format(CANNOT_DELETE_TIME_IN_USE, id));
        }
        reservationTimeService.deleteTime(id);
    }

    @Transactional
    public void deleteTheme(Long id) {
        if (reservationService.hasReservationsByThemeId(id)) {
            throw new BusinessRuleViolationException(String.format(CANNOT_DELETE_THEME_IN_USE, id));
        }
        themeService.deleteTheme(id);
    }

    @Transactional
    public ReservationPayment addReservation(ReservationRequest request) {
        ReservationTime reservationTime = reservationTimeService.findById(request.timeId());
        Theme theme = themeService.findById(request.themeId());

        Reservation reservation = new Reservation(
                request.name(),
                request.date(),
                reservationTime,
                theme
        );

        if (reservation.isPast(LocalDateTime.now())) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_REJECTED);
        }

        Reservations existing = reservationService.findByDateAndThemeId(request.date(), theme.getId());
        if (existing.isOccupied(reservationTime)) {
            throw new ConflictException(ALREADY_EXISTS_ADD_RESERVATION);
        }

        Reservation saved = reservationService.addReservation(reservation);
        Payment payment = paymentService.createOrder(saved.getId(), PAYMENT_AMOUNT);
        return new ReservationPayment(saved, payment);
    }

    @Transactional
    public Reservation confirmReservation(String orderId, PaymentConfirmRequest request) {
        Payment payment = paymentService.confirm(request.paymentKey(), orderId, request.amount());
        reservationService.confirm(payment.getReservationId());
        return reservationService.findReservation(payment.getReservationId())
                .orElseThrow(() -> NotFoundException.reservation(payment.getReservationId()));
    }

    @Transactional
    public Reservation updateMyReservation(Long id, String name, ReservationUpdateRequest request) {
        Reservation existing = reservationService.findMyReservation(id, name);
        LocalDateTime now = LocalDateTime.now();

        if (existing.isPast(now)) {
            throw new BusinessRuleViolationException(EXPIRED_RESERVATION_UPDATE_REJECTED);
        }

        ReservationTime newTime = reservationTimeService.findById(request.timeId());

        Reservation updated = new Reservation(
                id,
                existing.getName(),
                request.date(),
                newTime,
                existing.getTheme(),
                existing.getReservationStatus()
        );

        if (updated.isPast(now)) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_UPDATE_REJECTED);
        }

        Reservations others = reservationService
                .findByDateAndThemeId(request.date(), existing.getThemeId())
                .excluding(id);
        if (others.isOccupied(newTime)) {
            throw new ConflictException(ALREADY_EXISTS_ADD_RESERVATION);
        }

        return reservationService.updateReservation(updated);
    }

    @Transactional
    public void cancelMyReservation(Long id, String name) {
        Reservation reservation = reservationService.findMyReservation(id, name);
        if (reservation.isPast(LocalDateTime.now())) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_CANCEL_REJECTED);
        }
        reservationWaitingService.findEarliestByReservationId(reservation.getId())
                .ifPresentOrElse(
                        waiting -> promoteToReservation(reservation, waiting),
                        () -> reservationService.deleteReservation(reservation.getId())
                );
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationService.findReservation(id)
                .orElseThrow(() -> NotFoundException.reservation(id));
        if (reservation.isPast(LocalDateTime.now())) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_DELETE_REJECTED);
        }
        reservationWaitingService.findEarliestByReservationId(reservation.getId())
                .ifPresentOrElse(
                        waiting -> promoteToReservation(reservation, waiting),
                        () -> reservationService.deleteReservation(reservation.getId())
                );
    }

    private void promoteToReservation(Reservation reservation, ReservationWaiting waiting) {
        reservationService.transferWithPendingStatus(reservation.getId(), waiting.getName());
        reservationWaitingService.deleteById(waiting.getId());
    }

    @Transactional
    public WaitingWithOrder addWaiting(ReservationWaitingRequest request) {
        ReservationTime reservationTime = reservationTimeService.findById(request.timeId());
        Theme theme = themeService.findById(request.themeId());
        Reservations reservations = reservationService.findByDateAndThemeId(request.date(), theme.getId());
        Reservation reservation = reservations.findByTime(reservationTime);

        if (reservation.isPast(LocalDateTime.now())) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_WAITING_REJECTED);
        }
        if (reservationWaitingService.existBy(request.name(), reservation.getId())) {
            throw new BusinessRuleViolationException(ALREADY_WAITING);
        }

        ReservationWaiting reservationWaiting = new ReservationWaiting(
                request.name(),
                LocalDateTime.now(),
                reservation
        );

        return reservationWaitingService.addWaiting(reservationWaiting);
    }

    public List<TimeWithStatusResponse> getTimesWithAvailability(LocalDate date, Long themeId) {
        List<ReservationTime> times = reservationTimeService.getReservationTimes();
        Reservations reservations = reservationService.findByDateAndThemeId(date, themeId);

        return times.stream()
                .map(time -> TimeWithStatusResponse.from(time, reservations.isOccupied(time)))
                .toList();
    }
}
