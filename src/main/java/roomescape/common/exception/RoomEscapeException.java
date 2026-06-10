package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public class RoomEscapeException extends RuntimeException {
    private final ErrorCode errorCode;

    public RoomEscapeException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getErrorStatus() {
        return errorCode.getHttpStatus();
    }
}
