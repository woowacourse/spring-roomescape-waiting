package roomescape.payment.exception;

import org.springframework.http.HttpStatus;

public class OutboundRateLimitExceededException extends PaymentFailureException {

    public OutboundRateLimitExceededException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "OUTBOUND_RATE_LIMITED",
                "토스 결제 서버 호출 한도를 초과해 요청을 보내지 않았습니다. 잠시 후 다시 시도해 주세요.");
    }
}
