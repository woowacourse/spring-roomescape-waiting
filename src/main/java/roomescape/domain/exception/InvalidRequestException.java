package roomescape.domain.exception;

import roomescape.exception.RoomescapeException;

public class InvalidRequestException extends RoomescapeException {

    public InvalidRequestException(final String message) {
        super(message);
    }
}
