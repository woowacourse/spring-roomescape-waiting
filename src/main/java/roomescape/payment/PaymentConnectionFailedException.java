package roomescape.payment;

/**
 * 토스 연결 자체가 안 된 경우(거부/연결 타임아웃)의 도메인 예외. 요청 바이트가 토스에 도달하지 않았으므로 "토스가 처리했을 가능성"은 없다 — 안전하게 재시도할 수
 * 있는, 확정된 실패다.
 */
public class PaymentConnectionFailedException extends RuntimeException {

    public PaymentConnectionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
