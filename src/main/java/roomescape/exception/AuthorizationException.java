package roomescape.exception;

public class AuthorizationException extends RuntimeException {
    private final String errorCode;
    private final ExceptionResponse exceptionResponse;

    public AuthorizationException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorCode = errorType.getErrorCode();
        this.exceptionResponse = ExceptionResponse.of(errorType);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public ExceptionResponse getExceptionResponse() {
        return exceptionResponse;
    }
}
