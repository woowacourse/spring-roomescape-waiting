package roomescape.controller.exception;

import roomescape.exception.RoomescapeException;

public class AuthenticationException extends RoomescapeException {

    public AuthenticationException(final String message) {
        super(message);
    }
}
