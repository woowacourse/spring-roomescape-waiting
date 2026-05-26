package roomescape.exception;

public abstract class RoomescapeException extends RuntimeException {

    private final ErrorCode errorCode;

    protected RoomescapeException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
