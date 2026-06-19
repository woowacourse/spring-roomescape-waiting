package roomescape.dto.response;

import roomescape.domain.PaymentResult;

public record PaymentConfirmResponse(
        String paymentKey,
        String orderId,
        Long approvedAmount
) {
    public static PaymentConfirmResponse from(PaymentResult paymentResult) {
        return new PaymentConfirmResponse(
                paymentResult.paymentKey(),
                paymentResult.orderId(),
                paymentResult.approvedAmount()
        );
    }
}
