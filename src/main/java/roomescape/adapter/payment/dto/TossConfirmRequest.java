package roomescape.adapter.payment.dto;

/**
 * Toss 결제 승인 요청 바디. 세 필드 모두 필수.
 */
public record TossConfirmRequest(String paymentKey, String orderId, long amount) {
}
