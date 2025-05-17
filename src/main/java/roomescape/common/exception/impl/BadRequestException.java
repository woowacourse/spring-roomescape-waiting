package roomescape.common.exception.impl;

import roomescape.common.exception.RoomescapeException;

public class BadRequestException extends RoomescapeException {
    public BadRequestException(final String message) {
        super(message);
    }
}
