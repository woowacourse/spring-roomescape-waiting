package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.payment.PaymentResult;

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
        return reservationService.startPaymentConfirmation(orderId);
    }

    @Transactional
    public Reservation confirmPayment(String orderId, PaymentResult result) {
        return reservationService.confirmPayment(orderId, result.paymentKey());
    }

    @Transactional
    public Reservation releasePaymentConfirmation(String orderId) {
        return reservationService.releasePaymentConfirmation(orderId);
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

}
