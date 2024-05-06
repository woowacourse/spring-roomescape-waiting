package roomescape.service.exception;

import roomescape.exception.RoomescapeException;

public class DuplicateTimeException extends RoomescapeException {

    public DuplicateTimeException(final String message) {
        super(message);
    }
}
