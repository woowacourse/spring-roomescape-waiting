package roomescape.system.exception.model;

import roomescape.system.exception.error.ErrorType;

public class ValidateException extends CustomException {
    public ValidateException(final ErrorType errorType, final String message) {
        super(errorType, message);
    }
}
