package roomescape.infrastructure.payment;

record TossConfirmResponse(
        String paymentKey,
        String orderId,
        long totalAmount
) {
}
