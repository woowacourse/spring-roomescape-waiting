package roomescape.global;

public class RoomEscapeException extends RuntimeException {
    private final ErrorCode errorCode;

    public RoomEscapeException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public RoomEscapeException(ErrorCode errorCode, Object... args) {
        super(String.format(errorCode.message(), args));
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
