package roomescape.global.exception.model;

import roomescape.global.exception.error.ErrorType;

public class AssociatedDataExistsException extends CustomException {

    public AssociatedDataExistsException(final ErrorType errorType, final String message) {
        super(errorType, message);
    }
}
