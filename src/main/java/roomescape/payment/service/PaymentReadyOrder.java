package roomescape.payment.service;

import roomescape.payment.domain.PaymentOrder;

public record PaymentReadyOrder(
        String orderId,
        long amount,
        String orderName,
        String customerName
) {

    public static PaymentReadyOrder from(PaymentOrder paymentOrder) {
        return new PaymentReadyOrder(
                paymentOrder.getOrderId(),
                paymentOrder.getAmount(),
                "Roomflow 방탈출 예약",
                paymentOrder.getName()
        );
    }
}
