package roomescape.reservation.application.dto;

import roomescape.reservation.domain.PaymentOrder;

public record PaymentReadyResult(
        String orderId,
        Long amount
) {

    public static PaymentReadyResult from(PaymentOrder paymentOrder) {
        return new PaymentReadyResult(
                paymentOrder.getOrderId().value(),
                paymentOrder.getAmount().value()
        );
    }

    public static PaymentReadyResult from(String orderId, Long amount) {
        if (orderId == null || amount == null) {
            return null;
        }

        return new PaymentReadyResult(orderId, amount);
    }
}
