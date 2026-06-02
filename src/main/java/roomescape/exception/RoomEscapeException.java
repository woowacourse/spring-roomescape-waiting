package roomescape.exception;

public abstract class RoomEscapeException extends RuntimeException {

    protected RoomEscapeException(String message) {
        super(message);
    }
}
