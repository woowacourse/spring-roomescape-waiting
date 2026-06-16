package roomescape.infra.toss.dto;

public record TossPaymentRequest(String paymentKey, String orderId, Long amount) {
}
