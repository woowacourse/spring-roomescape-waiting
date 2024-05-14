package roomescape.global.exception.model;

import roomescape.global.exception.error.ErrorType;

public class DataDuplicateException extends CustomException {

    public DataDuplicateException(final ErrorType errorType, final String message) {
        super(errorType, message);
    }
}
