package roomescape.global.exception.impl;

import roomescape.global.exception.RoomescapeException;

public class ForbiddenException extends RoomescapeException {
    public ForbiddenException(final String message) {
        super(message);
    }
}
