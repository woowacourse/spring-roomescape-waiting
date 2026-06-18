package roomescape.payment.domain;

public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        String idempotencyKey,
        int amount
) {
}
