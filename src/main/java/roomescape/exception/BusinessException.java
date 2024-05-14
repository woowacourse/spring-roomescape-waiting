package roomescape.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final ExceptionResponse exceptionResponse;

    public BusinessException(ErrorType errorType) {
        super(errorType.getMessage());
        this.httpStatus = errorType.getHttpStatus();
        this.exceptionResponse = new ExceptionResponse(errorType.getMessage());
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ExceptionResponse getExceptionResponse() {
        return exceptionResponse;
    }
}
