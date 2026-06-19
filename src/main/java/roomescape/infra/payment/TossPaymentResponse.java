package roomescape.infra.payment;

import roomescape.payment.PaymentResult;

public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        Long totalAmount,
        String method,
        String approvedAt
) {

    public PaymentResult toResult() {
        return new PaymentResult(paymentKey, orderId, totalAmount, status);
    }
}
