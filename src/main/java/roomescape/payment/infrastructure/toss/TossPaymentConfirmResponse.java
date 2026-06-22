package roomescape.payment.infrastructure.toss;

record TossPaymentConfirmResponse(
        String paymentKey,
        String orderId,
        Integer totalAmount
) {
}
