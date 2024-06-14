package roomescape.system.exception.model;

import roomescape.system.exception.error.ErrorType;

public class AssociatedDataExistsException extends CustomException {
    public AssociatedDataExistsException(final ErrorType errorType, final String message) {
        super(errorType, message);
    }
}
