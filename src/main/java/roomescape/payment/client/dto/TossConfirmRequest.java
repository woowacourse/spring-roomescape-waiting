package roomescape.payment.client.dto;

public record TossConfirmRequest(String paymentKey, String orderId, Long amount) {
}
