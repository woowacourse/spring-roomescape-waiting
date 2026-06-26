package roomescape.payment;

/**
 * 응답 읽기 단계 실패(read timeout). 토스에 요청은 닿았으나 응답을 받지 못해
 * 승인 여부가 불명확한 상태다. "실패"로 단정하지 말고 "확인 필요"로 다뤄야 하며,
 * 같은 Idempotency-Key로 재시도해 결과를 수렴시킨다.
 */
public class PaymentResultUnknownException extends RuntimeException {
    public PaymentResultUnknownException(String message, Throwable cause) {
        super(message, cause);
    }
}
