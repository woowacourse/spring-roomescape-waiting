package roomescape.exception.server;

/**
 * 토스에 연결조차 못 한 상태(연결 거부/DNS 실패). 요청이 도달하지 않았으므로 결제는 진행되지 않았다 — 안전하게 정리·재시도할 수 있다.
 */
public class PaymentConnectionException extends RoomEscapeServerException {
    public PaymentConnectionException(String message) {
        super(message);
    }
}
