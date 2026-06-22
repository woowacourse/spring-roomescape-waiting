package roomescape.payment.domain;

/**
 * 결제 승인 결과 도메인 모델. PG 응답 DTO를 어댑터가 이 모델로 번역해 애플리케이션 계층에 전달한다.
 */
public record PaymentResult(String paymentKey, String orderId, String status, long totalAmount) {
}
