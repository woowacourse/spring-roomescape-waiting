package roomescape.service.dto;

import roomescape.domain.Payment;
import roomescape.domain.PaymentStatus;

public record BookingPaymentInfo(
        String orderId,
        Long amount,
        String paymentKey,
        PaymentStatus status,
        String failureCode,
        String failureMessage
) {

    public static BookingPaymentInfo from(Payment payment) {
        if (payment == null) {
            return null;
        }
        return new BookingPaymentInfo(
                payment.getOrderId(),
                payment.getAmount(),
                payment.getPaymentKey(),
                payment.getStatus(),
                payment.getFailureCode(),
                payment.getFailureMessage()
        );
    }
}
