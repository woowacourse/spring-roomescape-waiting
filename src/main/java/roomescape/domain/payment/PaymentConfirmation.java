package roomescape.domain.payment;

public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        long amount
) {
}
