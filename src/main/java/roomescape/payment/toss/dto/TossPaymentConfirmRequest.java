package roomescape.payment.toss.dto;

public record TossPaymentConfirmRequest(String paymentKey, String orderId, Long amount) {
}
