package roomescape.exception;

public class NotFoundException extends RuntimeException {

    private final String errorCode;
    private final ExceptionResponse exceptionResponse;

    public NotFoundException(ErrorType errorType) {
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
