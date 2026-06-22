package roomescape.payment;

/**
 * 응답을 읽는 단계에서 실패한 경우(read timeout)의 도메인 예외. 요청은 토스에 도달했을 수 있어 이미 승인이 끝났을 가능성이 있다 — "결제 실패"로 단정하지
 * 않고, 같은 멱등키로 다시 확인(재호출)해야 하는 상태다.
 */
public class PaymentResultUnknownException extends RuntimeException {

    public PaymentResultUnknownException(String message, Throwable cause) {
        super(message, cause);
    }
}
