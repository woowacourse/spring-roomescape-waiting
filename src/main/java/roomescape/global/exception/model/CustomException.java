package roomescape.global.exception.model;

import roomescape.global.exception.error.ErrorType;

public class CustomException extends RuntimeException {
    private final ErrorType errorType;

    public CustomException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public CustomException(final ErrorType errorType, final String message, final Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
