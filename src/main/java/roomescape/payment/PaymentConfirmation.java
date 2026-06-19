package roomescape.payment;

public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        String idempotencyKey,
        Long amount
) {
}
