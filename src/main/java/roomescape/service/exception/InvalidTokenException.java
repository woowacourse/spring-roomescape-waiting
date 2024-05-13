package roomescape.service.exception;

import roomescape.exception.RoomescapeException;

public class InvalidTokenException extends RoomescapeException {

    public InvalidTokenException(final String message) {
        super(message);
    }
}
