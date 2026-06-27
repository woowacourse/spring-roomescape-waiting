package roomescape.ratelimit;

import org.springframework.http.HttpStatus;
import roomescape.domain.payment.PaymentException;

public class OutboundRateLimitException extends PaymentException {

    public OutboundRateLimitException(long retryAfterSeconds) {
        super(
            HttpStatus.TOO_MANY_REQUESTS,
            "OUTBOUND_RATE_LIMIT_EXCEEDED",
            "결제 서버 호출 한도를 초과했습니다. %d초 후 다시 시도해주세요."
                .formatted(retryAfterSeconds)
        );
    }
}
