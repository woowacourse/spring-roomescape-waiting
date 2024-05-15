package roomescape.exception;

public class RoomescapeException extends RuntimeException {
    private final RoomescapeErrorCode errorCode;
    private final String message;

    public RoomescapeException(RoomescapeErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = null;
    }

    public RoomescapeException(RoomescapeErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public RoomescapeErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        if (message == null) {
            return errorCode.getMessage();
        }
        return message;
    }
}
