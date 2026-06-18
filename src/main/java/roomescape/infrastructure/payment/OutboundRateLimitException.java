package roomescape.infrastructure.payment;

import roomescape.exception.RoomescapeBaseException;

public class OutboundRateLimitException extends RoomescapeBaseException {
    private final long retryAfterSeconds;

    public OutboundRateLimitException(long retryAfterSeconds) {
        super("외부 결제 승인 요청이 일시적으로 많습니다. 잠시 후 다시 시도해주세요.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
