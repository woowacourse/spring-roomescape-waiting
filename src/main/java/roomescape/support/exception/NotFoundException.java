package roomescape.support.exception;

import roomescape.support.exception.errors.Errors;

public class NotFoundException extends RoomescapeException {

    public NotFoundException(Errors errors, Object... args) {
        super(errors, args);
    }
}
