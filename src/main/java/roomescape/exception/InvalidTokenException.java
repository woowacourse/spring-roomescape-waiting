package roomescape.exception;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException() {
        super(ErrorType.INVALID_TOKEN);
    }
}
