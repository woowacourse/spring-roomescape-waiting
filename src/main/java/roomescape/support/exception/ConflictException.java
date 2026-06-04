package roomescape.support.exception;

import roomescape.support.exception.errors.Errors;

public class ConflictException extends RoomescapeException {

    public ConflictException(Errors errors, Object... args) {
        super(errors, args);
    }
}
