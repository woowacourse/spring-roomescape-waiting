package roomescape.ratelimit;

import org.springframework.http.HttpStatus;
import roomescape.domain.payment.PaymentException;

public class PaymentRateLimitExceededException extends PaymentException {

    public PaymentRateLimitExceededException() {
        super(
            HttpStatus.TOO_MANY_REQUESTS,
            "PAYMENT_RATE_LIMIT_EXCEEDED",
            "결제 서버 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요."
        );
    }
}
