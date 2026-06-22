package roomescape.payment;

public class OutboundRateLimitException extends RuntimeException {

    private final long retryAfterSeconds;

    public OutboundRateLimitException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
