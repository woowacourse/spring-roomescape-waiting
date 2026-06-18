package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentStatus;

@Service
public class PaymentReservationService {

    private final ReservationService reservationService;

    public PaymentReservationService(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Transactional
    public Reservation preparePaymentConfirmation(String orderId, Long amount) {
        reservationService.lockByOrderId(orderId);
        Reservation reservation = reservationService.findByOrderId(orderId);
        validatePaymentRequest(reservation, amount);
        return reservation;
    }

    @Transactional
    public Reservation confirmPayment(String orderId, PaymentResult result, Long amount) {
        validatePaymentResult(result, orderId, amount);
        return reservationService.confirmPayment(orderId, result.paymentKey());
    }

    @Transactional
    public Reservation markPaymentUnknown(String orderId) {
        return reservationService.markPaymentUnknown(orderId);
    }

    private void validatePaymentRequest(Reservation reservation, Long amount) {
        if (!reservation.needsPaymentConfirmation()) {
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
}
