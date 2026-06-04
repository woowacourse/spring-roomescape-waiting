package roomescape.common.exception;

import roomescape.common.exception.errors.Errors;

public class InternalServerException extends RoomescapeException {

    public InternalServerException(Errors errors, Object... args) {
        super(errors, args);
    }
}
