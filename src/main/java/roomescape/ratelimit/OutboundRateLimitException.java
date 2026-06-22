package roomescape.ratelimit;

public class OutboundRateLimitException extends RuntimeException {

    private final long retryAfterSeconds;

    public OutboundRateLimitException(long retryAfterSeconds) {
        super("외부 결제 승인 호출 한도를 초과했습니다.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
