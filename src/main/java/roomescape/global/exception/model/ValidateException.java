package roomescape.global.exception.model;

import roomescape.global.exception.error.ErrorType;

public class ValidateException extends CustomException {

    public ValidateException(final ErrorType errorType, final String message) {
        super(errorType, message);
    }
}
