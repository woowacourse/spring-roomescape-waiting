package roomescape.infra.toss.dto;

public record TossPaymentResponse(String paymentKey, String orderId, Long totalAmount) {
}
