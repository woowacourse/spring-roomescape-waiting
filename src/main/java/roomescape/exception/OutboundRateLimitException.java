package roomescape.exception;

public class OutboundRateLimitException extends RoomescapeException {

    private final long retryAfterSeconds;

    public OutboundRateLimitException(long retryAfterSeconds) {
        super("외부 결제 API 호출 한도를 초과했습니다. retryAfterSeconds=" + retryAfterSeconds);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long retryAfterSeconds() {
        return retryAfterSeconds;
    }
}
