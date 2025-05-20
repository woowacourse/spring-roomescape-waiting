package roomescape.common.exception.impl;

import roomescape.common.exception.RoomescapeException;

public class NotFoundException extends RoomescapeException {
    public NotFoundException(final String message) {
        super(message);
    }
}
