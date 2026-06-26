package roomescape.payment;

public class OutboundRateLimitException extends RuntimeException {

    private final long retryAfterSeconds;

    public OutboundRateLimitException(long retryAfterSeconds) {
        super("결제 요청이 잠시 제한되었습니다. " + retryAfterSeconds + "초 후 다시 시도해 주세요.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
