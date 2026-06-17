package roomescape.feature.payment;

/**
 * 읽기 타임아웃 등 느린 응답: 요청은 전송됐으나 응답을 제때 받지 못했다.
 *
 * 결제 성공 여부가 '불명'이므로 실패로 단정해서는 안 된다.
 * 재시도가 소진되면 실제 결제 상태를 조회(reconciliation)해 확정하고, 그래도 불명이면 '확인 중'으로 표면화한다.
 */
public class PaymentTimeoutException extends PaymentClientException {

    public PaymentTimeoutException(Throwable cause) {
        super("결제 결과를 확인하지 못했습니다.", cause);
    }
}
