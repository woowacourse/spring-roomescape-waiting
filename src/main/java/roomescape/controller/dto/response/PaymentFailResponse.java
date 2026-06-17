package roomescape.controller.dto.response;

import roomescape.payment.PaymentFailure;

public record PaymentFailResponse(
        String code,
        String message,
        String orderId
) {

    public static PaymentFailResponse from(PaymentFailure failure) {
        return new PaymentFailResponse(failure.code(), failure.message(), failure.orderId());
    }
}
