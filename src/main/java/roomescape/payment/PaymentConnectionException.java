package roomescape.payment;

/**
 * 연결 단계 실패(연결 거부 / connect timeout). 토스에 닿지도 못한 상태이므로
 * "승인 안 됨"이 확실하고, 같은 주문을 그대로 재시도해도 안전하다.
 */
public class PaymentConnectionException extends RuntimeException {
    public PaymentConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
