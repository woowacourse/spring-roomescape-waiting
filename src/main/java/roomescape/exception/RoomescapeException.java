package roomescape.exception;

public abstract class RoomescapeException extends RuntimeException {
    
    private final String errorCode;

    public RoomescapeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public RoomescapeException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
