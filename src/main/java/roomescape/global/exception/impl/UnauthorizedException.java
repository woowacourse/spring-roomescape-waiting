package roomescape.global.exception.impl;

import roomescape.global.exception.RoomescapeException;

public class UnauthorizedException extends RoomescapeException {
    public UnauthorizedException(final String message) {
        super(message);
    }
}
