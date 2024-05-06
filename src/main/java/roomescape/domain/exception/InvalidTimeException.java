package roomescape.domain.exception;

import roomescape.exception.RoomescapeException;

public class InvalidTimeException extends RoomescapeException {

    public InvalidTimeException(final String message) {
        super(message);
    }
}
