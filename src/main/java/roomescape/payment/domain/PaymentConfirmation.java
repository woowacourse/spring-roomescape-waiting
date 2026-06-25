package roomescape.payment.domain;

/**
 * 결제 승인 요청 도메인 모델. 어떤 PG사도 모르는 순수 모델이며, 어댑터가 PG 전용 DTO로 번역한다.
 * idempotencyKey는 주문당 고정값이라 재시도해도 같은 키로 호출되어 이중 승인을 막는다.
 */
public record PaymentConfirmation(String orderId, String paymentKey, long amount, String idempotencyKey) {
}
