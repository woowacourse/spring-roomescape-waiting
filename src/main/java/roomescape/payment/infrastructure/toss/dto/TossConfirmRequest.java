package roomescape.payment.infrastructure.toss.dto;

public record TossConfirmRequest(String paymentKey, String orderId, Long amount) {
}
