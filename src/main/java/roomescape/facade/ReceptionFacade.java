package roomescape.facade;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.response.ReceptionResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.payment.PaymentConfirmationResult;
import roomescape.payment.PaymentService;
import roomescape.service.PaymentReservationService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;
import roomescape.service.dto.request.ServiceReservationCreateRequest;

@Service
@Transactional(readOnly = true)
public class ReceptionFacade {

    private final ReservationService reservationService;
    private final WaitService waitService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final PaymentService paymentService;
    private final PaymentReservationService paymentReservationService;
    private final Clock clock;

    public ReceptionFacade(ReservationService reservationService, WaitService waitService,
                            ReservationTimeService reservationTimeService, ThemeService themeService,
                            PaymentService paymentService, PaymentReservationService paymentReservationService,
                            Clock clock) {
        this.reservationService = reservationService;
        this.waitService = waitService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.paymentService = paymentService;
        this.paymentReservationService = paymentReservationService;
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
                .map(r -> ReceptionResponse.from(r, 0L, r.getStatus().name()))
                .forEach(receptions::add);

        waitService.findByName(name).stream()
                .map(w -> ReceptionResponse.from(w, waitService.calculateOrder(w), ReservationStatus.WAITING.name()))
                .forEach(receptions::add);

        return receptions;
    }

    public List<ReceptionResponse> findAll() {
        List<ReceptionResponse> receptions = new ArrayList<>();

        reservationService.findAll().stream()
                .map(r -> ReceptionResponse.from(r, 0L, r.getStatus().name()))
                .forEach(receptions::add);

        waitService.findAll().stream()
                .map(w -> ReceptionResponse.from(w, waitService.calculateOrder(w), ReservationStatus.WAITING.name()))
                .forEach(receptions::add);

        return receptions;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ReceptionResponse confirmPayment(String paymentKey, String orderId, Long amount) {
        Reservation reservation = paymentReservationService.preparePaymentConfirmation(orderId, amount);

        PaymentConfirmationResult confirmationResult = paymentService.confirm(paymentKey, orderId,
                reservation.getIdempotencyKey(), amount);
        if (confirmationResult.unknown()) {
            paymentReservationService.markPaymentUnknown(orderId);
            throw new RoomEscapeException(DomainErrorCode.PAYMENT_UNKNOWN);
        }
        if (confirmationResult.failed()) {
            paymentReservationService.releasePaymentConfirmation(orderId);
            throw new RoomEscapeException(confirmationResult.failureCode());
        }

        Reservation confirmed = paymentReservationService.confirmPayment(orderId, confirmationResult.paymentResult());
        return ReceptionResponse.from(confirmed, 0L, ReservationStatus.CONFIRMED.name());
    }

    @Transactional
    public void failPayment(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }
        Optional<Reservation> pendingReservation = reservationService.findPendingByOrderId(orderId);
        reservationService.deletePendingByOrderId(orderId);
        pendingReservation.ifPresent(this::promoteNextWait);
    }

    @Transactional
    public void deleteReservation(Long id) {
        reservationService.lockById(id);
        Reservation reservation = reservationService.findReservation(id);
        reservation.validateDeletable(LocalDateTime.now(clock));
        reservationService.delete(id);

        waitService.findBySlot(reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId())
                .findFirst()
                .ifPresent(this::promoteFirstWait);
    }

    @Transactional
    public void deleteWait(Long id) {
        Wait wait = waitService.findWait(id);
        wait.validateDeletable(LocalDateTime.now(clock));
        waitService.delete(id);
    }

    @Scheduled(fixedDelayString = "${roomescape.pending-payment-cleanup-delay-ms:60000}")
    @Transactional
    public void deleteExpiredPendingPayments() {
        deleteExpiredPendingPayments(LocalDateTime.now(clock));
    }

    private ReceptionResponse saveReservationOrWait(ServiceReservationCreateRequest request,
                                                    ReservationTime reservationTime, Theme theme) {
        deleteExpiredPendingPayments(LocalDateTime.now(clock));
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

    private void promoteFirstWait(Wait firstOrder) {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest(firstOrder.getName(),
                firstOrder.getReservationDate(), firstOrder.getTime().getId(), firstOrder.getTheme().getId());

        reservationService.savePending(request, firstOrder.getTime(), firstOrder.getTheme());
        waitService.delete(firstOrder.getId());
    }

    private void promoteNextWait(Reservation reservation) {
        waitService.findBySlot(reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId())
                .findFirst()
                .ifPresent(this::promoteFirstWait);
    }

    private void deleteExpiredPendingPayments(LocalDateTime now) {
        reservationService.findExpiredPendingPayments(now)
                .forEach(this::deleteExpiredPendingPayment);
    }

    private void deleteExpiredPendingPayment(Reservation reservation) {
        reservationService.deletePendingByOrderId(reservation.getOrderId());
        promoteNextWait(reservation);
    }
}
