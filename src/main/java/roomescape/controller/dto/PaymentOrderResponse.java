package roomescape.controller.dto;

import roomescape.service.dto.PaymentOrderResult;

public record PaymentOrderResponse(
        String orderId,
        Long amount,
        String orderName,
        String clientKey
) {
    public static PaymentOrderResponse from(PaymentOrderResult result, String clientKey) {
        return new PaymentOrderResponse(result.orderId(), result.amount(), result.orderName(), clientKey);
    }
}
