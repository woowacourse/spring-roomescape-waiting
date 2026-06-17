package roomescape.payment;

public record PaymentResult(
        String paymentKey,
        String orderId,
        Long totalAmount,
        String status
) {
}
