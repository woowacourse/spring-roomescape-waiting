package roomescape.global.exception;

public class RoomescapeException extends RuntimeException {
    private ErrorCode errorCode;

    public RoomescapeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
