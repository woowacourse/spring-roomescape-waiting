package roomescape.domain.exception;

import roomescape.exception.RoomescapeException;

public class InvalidNameException extends RoomescapeException {

    public InvalidNameException(final String message) {
        super(message);
    }
}
