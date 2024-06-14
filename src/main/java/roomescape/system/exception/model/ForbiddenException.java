package roomescape.system.exception.model;

import roomescape.system.exception.error.ErrorType;

public class ForbiddenException extends CustomException {
    public ForbiddenException(final ErrorType errorType, final String message) {
        super(errorType, message);
    }
}
