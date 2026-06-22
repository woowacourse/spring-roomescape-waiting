package roomescape.payment.infra.toss;

import roomescape.payment.application.exception.PaymentErrorCode;
import roomescape.payment.application.exception.PaymentException;
import roomescape.payment.domain.PaymentResult;

record TossConfirmResponse(
        String paymentKey,
        String orderId,
        Long totalAmount,
        String status
) {
    PaymentResult toResult() {
        if (totalAmount == null) {
            throw new PaymentException(PaymentErrorCode.UNKNOWN_GATEWAY_ERROR);
        }
        return new PaymentResult(paymentKey, orderId, totalAmount, status);
    }
}
