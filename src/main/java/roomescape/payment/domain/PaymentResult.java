package roomescape.payment.domain;

public record PaymentResult(
        String paymentKey,
        String orderId,
        String status,
        int totalAmount
) {
}
