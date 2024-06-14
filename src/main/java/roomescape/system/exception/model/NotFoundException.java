package roomescape.system.exception.model;

import roomescape.system.exception.error.ErrorType;

public class NotFoundException extends CustomException {
    public NotFoundException(final ErrorType errorType, final String message) {
        super(errorType, message);
    }
}
