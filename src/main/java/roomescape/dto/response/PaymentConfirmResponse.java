package roomescape.dto.response;

import roomescape.domain.payment.PaymentResult;

public record PaymentConfirmResponse(
        String paymentKey,
        String orderId,
        long amount
) {
    public static PaymentConfirmResponse from(PaymentResult result) {
        return new PaymentConfirmResponse(result.paymentKey(), result.orderId(), result.amount());
    }
}
