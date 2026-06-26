package roomescape.infrastructure.toss;

public class OutboundRateLimitException extends RuntimeException {
    private final long retryAfterSeconds;

    public OutboundRateLimitException(long retryAfterSeconds) {
        super("외부 결제 승인 서버 호출량이 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
