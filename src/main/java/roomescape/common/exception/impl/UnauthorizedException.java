package roomescape.common.exception.impl;

import roomescape.common.exception.RoomescapeException;

public class UnauthorizedException extends RoomescapeException {
    public UnauthorizedException(final String message) {
        super(message);
    }
}
