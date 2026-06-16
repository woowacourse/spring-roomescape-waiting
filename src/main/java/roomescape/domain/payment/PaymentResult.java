package roomescape.domain.payment;

/**
 * 승인 결과를 표현하는 도메인 모델(포트 출력). 어댑터가 토스 응답 DTO를 이 모델로 번역해 돌려준다.
 * 도메인/애플리케이션은 이 모델만 보고 토스 응답 형식을 모른다.
 */
public record PaymentResult(String paymentKey, String orderId, String status, long totalAmount) {
}
