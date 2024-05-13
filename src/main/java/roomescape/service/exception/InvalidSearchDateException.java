package roomescape.service.exception;

import roomescape.exception.RoomescapeException;

public class InvalidSearchDateException extends RoomescapeException {

    public InvalidSearchDateException(final String message) {
        super(message);
    }
}
