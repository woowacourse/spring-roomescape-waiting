package roomescape.controller.dto.payment;

import roomescape.payment.PaymentOrder;

public record PaymentOrderResponse(
        String clientKey,
        String orderId,
        String orderName,
        int amount,
        String successUrl,
        String failUrl
) {

    public static PaymentOrderResponse from(
            PaymentOrder order,
            String clientKey,
            String orderName,
            String successUrl,
            String failUrl
    ) {
        return new PaymentOrderResponse(
                clientKey,
                order.getOrderId(),
                orderName,
                order.getAmount(),
                successUrl,
                failUrl
        );
    }
}
