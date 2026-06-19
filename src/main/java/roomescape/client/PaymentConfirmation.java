package roomescape.client;

public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        Long amount,
        String idempotencyKey
) {
}
