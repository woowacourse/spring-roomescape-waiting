package roomescape.ratelimit;

import lombok.Getter;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;

@Getter
public class OutboundRateLimitException extends EscapeRoomException {

    private final long retryAfterSeconds;

    public OutboundRateLimitException(long retryAfterSeconds) {
        super(ErrorCode.PAYMENT_OUTBOUND_RATE_LIMITED);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
