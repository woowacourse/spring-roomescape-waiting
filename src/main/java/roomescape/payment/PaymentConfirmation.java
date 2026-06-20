package roomescape.payment;

public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        Long amount
) {
}