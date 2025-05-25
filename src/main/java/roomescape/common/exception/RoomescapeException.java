package roomescape.common.exception;

public class RoomescapeException extends IllegalArgumentException {

    private static final String ERROR_PREFIX = "[ERROR] ";

    public RoomescapeException(String message) {
        super(ERROR_PREFIX + message);
    }

    public RoomescapeException(String message, Throwable cause) {
        super(ERROR_PREFIX + message, cause);
    }
}
