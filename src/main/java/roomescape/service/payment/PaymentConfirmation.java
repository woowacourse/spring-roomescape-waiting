package roomescape.service.payment;

public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        Long amount
) {
}
