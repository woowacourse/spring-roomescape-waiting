package roomescape.payment;

public class TossRateLimitException extends RuntimeException {

    private final long retryAfterSeconds;

    public TossRateLimitException(long retryAfterSeconds) {
        super("결제 서버 요청이 한도를 초과했습니다. " + retryAfterSeconds + "초 후 다시 시도해 주세요.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
