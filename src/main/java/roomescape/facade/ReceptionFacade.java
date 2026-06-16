package roomescape.facade;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.response.ReceptionResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentService;
import roomescape.payment.PaymentStatus;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;
import roomescape.service.dto.request.ServiceReservationCreateRequest;

@Service
@Transactional(readOnly = true)
public class ReceptionFacade {

    private static final long PENDING_PAYMENT_TTL_MINUTES = 10L;

    private final ReservationService reservationService;
    private final WaitService waitService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final PaymentService paymentService;
    private final Clock clock;

    public ReceptionFacade(ReservationService reservationService, WaitService waitService,
                           ReservationTimeService reservationTimeService, ThemeService themeService,
                           PaymentService paymentService, Clock clock) {
        this.reservationService = reservationService;
        this.waitService = waitService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.paymentService = paymentService;
        this.clock = clock;
    }

    @Transactional
    public ReceptionResponse save(ServiceReservationCreateRequest request) {
        ReservationTime reservationTime = reservationTimeService.findReservationTime(request.timeId());
        Theme theme = themeService.findTheme(request.themeId());
        Reservation newReservation = new Reservation(request.name(), request.reservationDate(), reservationTime, theme);
        newReservation.validateCreatable(LocalDateTime.now(clock));

        return saveReservationOrWait(request, reservationTime, theme);
    }

    public List<ReceptionResponse> findByName(String name) {
        List<ReceptionResponse> receptions = new ArrayList<>();

        reservationService.findByName(name).stream()
                .map(r -> ReceptionResponse.from(r, 0L, ReservationStatus.CONFIRMED.name()))
                .forEach(receptions::add);

        waitService.findByName(name).stream()
                .map(w -> ReceptionResponse.from(w, waitService.calculateOrder(w), ReservationStatus.WAITING.name()))
                .forEach(receptions::add);

        return receptions;
    }

    public List<ReceptionResponse> findAll() {
        List<ReceptionResponse> receptions = new ArrayList<>();

        reservationService.findAll().stream()
                .map(r -> ReceptionResponse.from(r, 0L, ReservationStatus.CONFIRMED.name()))
                .forEach(receptions::add);

        waitService.findAll().stream()
                .map(w -> ReceptionResponse.from(w, waitService.calculateOrder(w), ReservationStatus.WAITING.name()))
                .forEach(receptions::add);

        return receptions;
    }

    @Transactional
    public ReceptionResponse confirmPayment(String paymentKey, String orderId, Long amount) {
        reservationService.lockByOrderId(orderId);
        Reservation lockedReservation = reservationService.findByOrderId(orderId);
        validatePaymentRequest(lockedReservation, amount);

        PaymentResult result = paymentService.confirm(paymentKey, orderId, amount);
        validatePaymentResult(result, orderId, amount);

        Reservation confirmed = reservationService.confirmPayment(orderId, result.paymentKey());
        return ReceptionResponse.from(confirmed, 0L, ReservationStatus.CONFIRMED.name());
    }

    @Transactional
    public void failPayment(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }
        reservationService.deletePendingByOrderId(orderId);
    }

    @Transactional
    public void deleteReservation(Long id) {
        reservationService.lockById(id);
        Reservation reservation = reservationService.findReservation(id);
        reservation.validateDeletable(LocalDateTime.now(clock));
        reservationService.delete(id);

        waitService.findBySlot(reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId())
                .findFirst()
                .ifPresent(this::confirmFirstWait);
    }

    @Transactional
    public void deleteWait(Long id) {
        Wait wait = waitService.findWait(id);
        wait.validateDeletable(LocalDateTime.now(clock));
        waitService.delete(id);
    }

    private ReceptionResponse saveReservationOrWait(ServiceReservationCreateRequest request,
                                                    ReservationTime reservationTime, Theme theme) {
        reservationService.deleteStalePendingBefore(LocalDateTime.now(clock).minusMinutes(PENDING_PAYMENT_TTL_MINUTES));
        Optional<Long> lockedId = reservationService.lockBySlot(request.reservationDate(), request.timeId(),
                request.themeId());
        Optional<Reservation> existing = lockedId.map(reservationService::findReservation);
        return existing.map(r -> {
            if (r.isPending()) {
                throw new RoomEscapeException(DomainErrorCode.SLOT_JUST_TAKEN);
            }
            if (r.isReservedBy(request.name())) {
                throw new RoomEscapeException(DomainErrorCode.DUPLICATED_RESERVATION);
            }
            Wait newWait = waitService.save(request.toWait(LocalDateTime.now(clock), reservationTime, theme));
            return ReceptionResponse.from(newWait, waitService.calculateOrder(newWait),
                    ReservationStatus.WAITING.name());
        }).orElseGet(() -> {
            Reservation saved = reservationService.savePending(request, reservationTime, theme);
            return ReceptionResponse.from(saved, 0L, ReservationStatus.PENDING.name());
        });
    }

    private void validatePaymentRequest(Reservation reservation, Long amount) {
        if (!reservation.isPending()) {
            throw new RoomEscapeException(DomainErrorCode.PAYMENT_ALREADY_PROCESSED);
        }
        if (!reservation.getAmount().equals(amount)) {
            throw new RoomEscapeException(DomainErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private void validatePaymentResult(PaymentResult result, String orderId, Long amount) {
        if (!PaymentStatus.DONE.name().equals(result.status()) || !result.orderId().equals(orderId)) {
            throw new RoomEscapeException(DomainErrorCode.PAYMENT_FAILED);
        }
        if (!result.approvedAmount().equals(amount)) {
            throw new RoomEscapeException(DomainErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private void confirmFirstWait(Wait firstOrder) {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest(firstOrder.getName(),
                firstOrder.getReservationDate(), firstOrder.getTime().getId(), firstOrder.getTheme().getId());

        reservationService.save(request, firstOrder.getTime(), firstOrder.getTheme());
        waitService.delete(firstOrder.getId());
    }
}
