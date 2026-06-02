package roomescape.exception;

public abstract class RoomescapeException extends RuntimeException {

    protected RoomescapeException(String message) {
        super(message);
    }
}
