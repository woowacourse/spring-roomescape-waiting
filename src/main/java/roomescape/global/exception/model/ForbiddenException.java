package roomescape.global.exception.model;

import roomescape.global.exception.error.ErrorType;

public class ForbiddenException extends CustomException {

    public ForbiddenException(final ErrorType errorType, final String message) {
        super(errorType, message);
    }
}
