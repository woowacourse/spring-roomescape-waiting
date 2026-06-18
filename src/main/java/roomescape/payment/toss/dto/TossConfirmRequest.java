package roomescape.payment.toss.dto;

/**
 * 토스 승인 요청 바디. 어댑터 안에서만 사용한다.
 */
public record TossConfirmRequest(String paymentKey, String orderId, long amount) {
}
