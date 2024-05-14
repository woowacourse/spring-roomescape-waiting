package roomescape.service.exception;

import roomescape.exception.RoomescapeException;

public class UnauthorizedException extends RoomescapeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
