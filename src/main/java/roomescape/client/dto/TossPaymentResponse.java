package roomescape.client.dto;

public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String status,
        Long totalAmount
) {
}
