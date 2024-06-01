package roomescape.global.exception.model;

import roomescape.global.exception.error.ErrorType;

public class InternalServerException extends CustomException {

    public InternalServerException(final ErrorType errorType, final String message) {
        super(errorType, message);
    }
}
