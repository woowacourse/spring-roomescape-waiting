package roomescape.common.exception.impl;

import roomescape.common.exception.RoomescapeException;

public class ForbiddenException extends RoomescapeException {
    public ForbiddenException(final String message) {
        super(message);
    }
}
