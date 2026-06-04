package roomescape.exception;

public class NotFoundException extends RoomescapeException {

    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
