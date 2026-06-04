package roomescape.exception;

public class DuplicateException extends RoomescapeException {

    public DuplicateException(String message) {
        super("DUPLICATE", message);
    }
}
