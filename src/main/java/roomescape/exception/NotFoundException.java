package roomescape.exception;

public class NotFoundException extends RoomescapeException {

    public NotFoundException(String detail) {
        super(ErrorCode.NOT_FOUND, detail);
    }
}
