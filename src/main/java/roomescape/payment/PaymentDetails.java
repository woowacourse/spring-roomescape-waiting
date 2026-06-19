package roomescape.payment;

public record PaymentDetails(
        String orderId,
        String paymentKey,
        Long amount,
        PaymentStatus status
) {
}
