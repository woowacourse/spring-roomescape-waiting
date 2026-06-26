package roomescape.payment.domain;

public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        Long amount,
        String idempotencyKey
) {
}
