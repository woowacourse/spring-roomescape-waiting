package roomescape.payment.infrastructure.toss;

record TossPaymentConfirmRequest(
        String paymentKey,
        String orderId,
        int amount
) {
}
