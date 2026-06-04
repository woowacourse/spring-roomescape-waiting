package roomescape.common.exception;

import roomescape.common.exception.errors.Errors;

public class NotFoundException extends RoomescapeException {

    public NotFoundException(Errors errors, Object... args) {
        super(errors, args);
    }
}
