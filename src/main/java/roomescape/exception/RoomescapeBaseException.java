package roomescape.exception;

public class RoomescapeBaseException extends RuntimeException {

    public RoomescapeBaseException(String message) {
        super(message);
    }

    public RoomescapeBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
