package roomescape.exception;

public class DatabaseException extends RoomEscapeException {

    public DatabaseException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
