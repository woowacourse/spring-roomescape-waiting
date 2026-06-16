package roomescape.payment.infra.toss;

record TossPaymentConfirmResponse(
        String paymentKey,
        String orderId,
        long totalAmount
) {
}
