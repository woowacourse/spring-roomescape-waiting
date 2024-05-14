package roomescape.global.exception.model;

import roomescape.global.exception.error.ErrorType;

public class NotFoundException extends CustomException {

    public NotFoundException(final ErrorType errorType, final String message) {
        super(errorType, message);
    }
}
