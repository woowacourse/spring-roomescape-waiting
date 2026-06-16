package roomescape.payment.client.dto;

public record TossPaymentResponse(String paymentKey, String orderId, String status, long totalAmount) {
}