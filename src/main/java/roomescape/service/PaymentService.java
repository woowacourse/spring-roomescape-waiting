package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Payment;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.PaymentRepository;
import roomescape.service.payment.PaymentAmountMismatchException;
import roomescape.service.payment.PaymentConfirmation;
import roomescape.service.payment.PaymentGatewayException;
import roomescape.service.payment.PaymentGateway;
import roomescape.service.payment.PaymentResult;

@Service
@Transactional
public class PaymentService {

    private static final long RESERVATION_AMOUNT = 20_000L;

    private final ReservationService reservationService;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(ReservationService reservationService, PaymentRepository paymentRepository,
                          PaymentGateway paymentGateway) {
        this.reservationService = reservationService;
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
    }

    public Payment createForReservation(String name, LocalDate date, Long timeId, Long themeId, LocalDateTime now) {
        Reservation reservation = reservationService.createPendingByUser(name, date, timeId, themeId, now);
        return paymentRepository.insert(Payment.ready(reservation.getId(), RESERVATION_AMOUNT));
    }

    public Payment resumeOrRetryForReservation(Long reservationId, String name, LocalDateTime now) {
        Reservation reservation = reservationService.findPendingByUser(reservationId, name, now);
        return paymentRepository.findLatestByReservationId(reservation.getId())
                .map(payment -> reuseOrCreatePayment(payment, reservation.getId()))
                .orElseGet(() -> paymentRepository.insert(Payment.ready(reservation.getId(), RESERVATION_AMOUNT)));
    }

    @Transactional(readOnly = true)
    public Reservation findReservationByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 결제입니다."));
        return reservationService.findById(payment.getReservationId());
    }

    @Transactional(readOnly = true)
    public Reservation findReservationByPaymentId(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 결제입니다."));
        return reservationService.findById(payment.getReservationId());
    }

    @Transactional(noRollbackFor = PaymentGatewayException.class)
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 결제입니다."));
        if (!payment.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(payment.getAmount(), amount);
        }
        if (!canConfirm(payment)) {
            throw new RoomescapeException(ErrorCode.PAYMENT_CONFIRMATION_NOT_ALLOWED,
                    "결제 대기 또는 확인 필요 상태의 결제만 승인할 수 있습니다.");
        }

        PaymentResult result;
        try {
            result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
        } catch (PaymentGatewayException e) {
            if (e.isDefinitiveFailure()) {
                paymentRepository.update(payment.fail(e.getCode(), e.getMessage()));
            }
            if (e.requiresConfirmationCheck()) {
                paymentRepository.update(payment.checkRequired(e.getCode(), e.getMessage()));
            }
            throw e;
        }
        if (result.status() != PaymentStatus.CONFIRMED) {
            throw new RoomescapeException(ErrorCode.PAYMENT_CONFIRMATION_NOT_ALLOWED,
                    "결제 승인 결과가 확정 상태가 아닙니다.");
        }
        paymentRepository.update(payment.confirm(result.paymentKey()));
        reservationService.confirmPayment(payment.getReservationId());
        return result;
    }

    public void fail(Long paymentId, String failureCode, String failureMessage) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 결제입니다."));
        if (payment.getStatus() != PaymentStatus.READY) {
            return;
        }
        paymentRepository.update(payment.fail(failureCode, failureMessage));
    }

    private Payment reuseOrCreatePayment(Payment payment, Long reservationId) {
        if (payment.getStatus() == PaymentStatus.READY) {
            return payment;
        }
        if (payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.CANCELED) {
            return paymentRepository.insert(Payment.ready(reservationId, RESERVATION_AMOUNT));
        }
        throw new RoomescapeException(ErrorCode.PAYMENT_RETRY_NOT_ALLOWED,
                "결제를 다시 시도할 수 없는 상태입니다.");
    }

    private boolean canConfirm(Payment payment) {
        return payment.getStatus() == PaymentStatus.READY || payment.getStatus() == PaymentStatus.CHECK_REQUIRED;
    }
}
