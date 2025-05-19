package roomescape.common.exception;

public class InvalidInputException extends CustomException {

    public InvalidInputException(String message) {
        super(message, ErrorCode.INVALID_INPUT);
    }
}
