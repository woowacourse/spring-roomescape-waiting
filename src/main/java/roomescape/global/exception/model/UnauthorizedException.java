package roomescape.global.exception.model;

import roomescape.global.exception.error.ErrorType;

public class UnauthorizedException extends CustomException {

    public UnauthorizedException(final ErrorType errorType, final String message) {
        super(errorType, message);
    }

    public UnauthorizedException(final ErrorType errorType, final String message, final Throwable cause) {
        super(errorType, message, cause);
    }
}
