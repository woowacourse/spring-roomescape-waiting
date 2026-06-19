package roomescape.domain;

/**
 * 결제 승인 요청을 표현하는 도메인 모델. 특정 PG사의 요청 포맷이 아니라 우리 도메인 언어로 표현한다.
 */
public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        Long amount
) {

}
