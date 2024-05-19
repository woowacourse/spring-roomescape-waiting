package roomescape.service.exception;

import roomescape.exception.RoomescapeException;

public class TimeNotFoundException extends RoomescapeException {

    public TimeNotFoundException(final String message) {
        super(message);
    }
}
