package roomescape.error;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final ErrorCode errorCode;

    public BusinessException(HttpStatus httpStatus, ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public BusinessException(HttpStatus httpStatus, ErrorCode errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
