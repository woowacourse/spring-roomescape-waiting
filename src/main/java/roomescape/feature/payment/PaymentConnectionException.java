package roomescape.feature.payment;

/**
 * 연결 실패: 요청이 토스에 전송되기 전에 실패했다(연결 거부·연결 타임아웃·DNS 실패 등).
 *
 * 결제가 일어났을 수 없음이 '확정'이므로, 재시도가 모두 소진되면 결제 실패로 단정해도 안전하다.
 */
public class PaymentConnectionException extends PaymentClientException {

    public PaymentConnectionException(Throwable cause) {
        super("결제 서버에 연결하지 못했습니다.", cause);
    }
}
