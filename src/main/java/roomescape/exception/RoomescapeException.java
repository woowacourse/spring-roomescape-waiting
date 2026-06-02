package roomescape.exception;

public class RoomescapeException extends RuntimeException {

    private final ErrorCode errorCode;

    public RoomescapeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
