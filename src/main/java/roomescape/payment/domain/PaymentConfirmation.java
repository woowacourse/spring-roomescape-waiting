package roomescape.payment.domain;

public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        long amount
) {
}
