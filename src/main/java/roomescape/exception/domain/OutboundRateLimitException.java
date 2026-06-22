package roomescape.exception.domain;

import roomescape.exception.RoomescapeException;
import roomescape.exception.code.RateLimitErrorCode;

public class OutboundRateLimitException extends RoomescapeException {

    public OutboundRateLimitException() {
        super(RateLimitErrorCode.OUTBOUND_RATE_LIMIT_EXCEEDED);
    }
}
