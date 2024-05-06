package roomescape.domain.exception;

import roomescape.exception.RoomescapeException;

public class InvalidDateException extends RoomescapeException {

    public InvalidDateException(final String message) {
        super(message);
    }
}
