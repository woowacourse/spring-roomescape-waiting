package roomescape.exception;

public class InvalidInputException extends RoomescapeException {

    public InvalidInputException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
