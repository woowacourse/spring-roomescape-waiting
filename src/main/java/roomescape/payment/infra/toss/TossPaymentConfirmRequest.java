package roomescape.payment.infra.toss;

record TossPaymentConfirmRequest(
        String paymentKey,
        String orderId,
        long amount
) {
}
