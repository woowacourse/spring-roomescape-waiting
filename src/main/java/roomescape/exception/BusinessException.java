package roomescape.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(final ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return errorCode.status();
    }

    public String getMessage() {
        return errorCode.message();
    }
}
