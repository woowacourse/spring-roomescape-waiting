package roomescape.payment;

public record PaymentResult(
        String paymentKey,
        String orderId,
        int totalAmount,
        String status
) {
}
