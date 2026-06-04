package roomescape.common.exception;

import roomescape.common.exception.errors.Errors;

public class BadRequestException extends RoomescapeException {

    public BadRequestException(Errors errors, Object... args) {
        super(errors, args);
    }
}
