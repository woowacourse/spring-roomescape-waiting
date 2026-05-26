package roomescape.common.exception;

import roomescape.common.exception.code.ErrorCode;

public class RoomEscapeException extends RuntimeException {
    private final ErrorCode errorCode;

    public RoomEscapeException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
