package roomescape.exception.PaymentException;

/**
 * read timeout/연결 실패 등으로 승인 결과를 확인하지 못한 상태.
 * "결제 실패"로 단정하지 않고, 멱등키로 안전하게 재확인·재시도할 수 있음을 의미한다.
 */
public class PaymentResultUnknownException extends RuntimeException {
    public PaymentResultUnknownException(String message) {
        super(message);
    }
}
