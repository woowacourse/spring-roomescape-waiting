package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public abstract class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;

    protected BusinessException(HttpStatus status, ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.status = status;
        this.errorCode = errorCode;
    }

    protected BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.errorCode = null;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
