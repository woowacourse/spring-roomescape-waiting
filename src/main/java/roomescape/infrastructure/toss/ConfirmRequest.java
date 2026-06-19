package roomescape.infrastructure.toss;

public record ConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
}