package roomescape.dto.request;

public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        Long amount,
        String idempotencyKey
) {
}
