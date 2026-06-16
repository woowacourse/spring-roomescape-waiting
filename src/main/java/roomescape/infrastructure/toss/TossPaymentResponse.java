package roomescape.infrastructure.toss;

public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        Long totalAmount
) {
}