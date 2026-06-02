package roomescape.exception;

public class RoomescapeException extends RuntimeException {

    private final ErrorCode errorCode;

    public RoomescapeException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }

    public RoomescapeException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDetail());
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
