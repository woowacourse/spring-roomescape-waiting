package roomescape.payment.toss.dto;

public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String status,
        long totalAmount
) {
}
