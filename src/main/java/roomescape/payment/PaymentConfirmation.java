package roomescape.payment;

/**
 * 승인 요청을 표현하는 도메인 모델(포트 입력). PaymentService가 만들어 PaymentGateway에 건넨다.
 * 토스 DTO가 아니라 도메인 언어로 표현되며, 어댑터가 토스 요청 형식으로 번역한다.
 */
public record PaymentConfirmation(String paymentKey, String orderId, long amount) {
}
