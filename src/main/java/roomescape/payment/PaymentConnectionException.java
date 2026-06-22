package roomescape.payment;

/**
 * 토스 연결 단계 실패(연결 거부/연결 타임아웃). 요청이 토스에 닿지 못한 상태라 결제는 생성되지 않았고,
 * 그래서 그대로 다시 시도해도 안전하다. "거절"도 "확인 필요"도 아닌, 단순 재시도 대상이다.
 */
public class PaymentConnectionException extends RuntimeException {

    public PaymentConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
