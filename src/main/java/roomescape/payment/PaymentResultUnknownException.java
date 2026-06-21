package roomescape.payment;

/**
 * 응답 읽기 단계 실패(read timeout). 요청은 토스에 닿았으나 응답만 유실된, "승인됐는지 모르는" 상태다.
 * 결과가 불명확하므로 "결제 실패"로 단정하지 않고, 사용자가 내역에서 결과를 확인하거나 재시도할 수 있게 한다.
 * 같은 멱등키로 재시도해도 이중 승인되지 않으므로 재시도가 안전하다.
 */
public class PaymentResultUnknownException extends RuntimeException {

    public PaymentResultUnknownException(String message, Throwable cause) {
        super(message, cause);
    }
}
