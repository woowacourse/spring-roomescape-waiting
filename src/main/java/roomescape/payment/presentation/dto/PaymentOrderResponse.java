package roomescape.payment.presentation.dto;

import roomescape.payment.application.dto.PaymentOrderResult;

public record PaymentOrderResponse(
        String orderId,
        long amount,
        String orderName,
        String clientKey
) {
    public static PaymentOrderResponse from(PaymentOrderResult result) {
        return new PaymentOrderResponse(
                result.orderId(),
                result.amount(),
                result.orderName(),
                result.clientKey()
        );
    }
}
