package roomescape.common.exception;

import roomescape.common.exception.errors.Errors;

public class ConflictException extends RoomescapeException {

    public ConflictException(Errors errors, Object... args) {
        super(errors, args);
    }
}
