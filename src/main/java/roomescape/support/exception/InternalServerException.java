package roomescape.support.exception;

import roomescape.support.exception.errors.Errors;

public class InternalServerException extends RoomescapeException {

    public InternalServerException(Errors errors, Object... args) {
        super(errors, args);
    }
}
