package roomescape.payment.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 토스 승인 응답 바디. 토스는 더 많은 필드를 주지만 우리가 쓰는 것만 받는다(나머지는 무시).
 * package-private — 어댑터 안에서 PaymentResult로 번역되고 사라진다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record TossPaymentResponse(String paymentKey, String orderId, String status, long totalAmount) {
}
