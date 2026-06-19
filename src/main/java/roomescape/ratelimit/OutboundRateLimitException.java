package roomescape.ratelimit;

import org.springframework.web.client.RestClientException;

public class OutboundRateLimitException extends RestClientException {

    private final long retryAfterSeconds;

    public OutboundRateLimitException(long retryAfterSeconds) {
        super("나가는 결제 승인 요청 한도를 초과했습니다.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
