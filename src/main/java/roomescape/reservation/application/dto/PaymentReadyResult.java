package roomescape.reservation.application.dto;

import roomescape.reservation.domain.Payment;

public record PaymentReadyResult(
        String orderId,
        Long amount
) {

    public static PaymentReadyResult from(Payment payment) {
        return new PaymentReadyResult(
                payment.getOrderId().value(),
                payment.getAmount().value()
        );
    }

    public static PaymentReadyResult from(String orderId, Long amount) {
        if (orderId == null || amount == null) {
            return null;
        }

        return new PaymentReadyResult(orderId, amount);
    }
}
