package roomescape.service.exception;

import roomescape.exception.RoomescapeException;

public class ThemeNotFoundException extends RoomescapeException {

    public ThemeNotFoundException(final String message) {
        super(message);
    }
}
