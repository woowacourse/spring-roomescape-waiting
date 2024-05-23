package roomescape.system.exception.model;

import roomescape.system.exception.error.ErrorType;

public class DataDuplicateException extends CustomException {
    public DataDuplicateException(final ErrorType errorType, final String message) {
        super(errorType, message);
    }
}
