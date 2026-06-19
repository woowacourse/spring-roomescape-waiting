package roomescape.infrastructure.payment;

record TossConfirmRequest(
        String paymentKey,
        String orderId,
        long amount
) {
}
