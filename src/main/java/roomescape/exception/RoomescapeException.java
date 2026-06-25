package roomescape.exception;

public abstract class RoomescapeException extends RuntimeException {

    protected RoomescapeException(String message) {
        super(message);
    }

    protected RoomescapeException(String message, Throwable cause) {
        super(message, cause);
    }
}
