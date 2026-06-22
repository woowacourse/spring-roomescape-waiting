package roomescape.exception;

public class PaymentRateLimitException extends RuntimeException {

    public PaymentRateLimitException() {
        super("결제 서버의 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
    }
}