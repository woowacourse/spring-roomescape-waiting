package roomescape.application.payment.model;

/**
 * 결제 승인 결과를 표현하는 도메인 모델. PG사 응답을 어댑터가 이 모델로 번역해, 외부 스키마가 도메인으로 새지 않는다.
 */
public record PaymentResult(
        String paymentKey,
        String orderId,
        PaymentStatus status,
        Long approvedAmount
) {

}
