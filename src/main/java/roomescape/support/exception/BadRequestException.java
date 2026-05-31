package roomescape.support.exception;

import roomescape.support.exception.errors.Errors;

public class BadRequestException extends RoomescapeException {

    public BadRequestException(Errors errors, Object... args) {
        super(errors, args);
    }
}
