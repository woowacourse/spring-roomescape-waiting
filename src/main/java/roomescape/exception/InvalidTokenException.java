package roomescape.exception;

public class InvalidTokenException extends RuntimeException {
    private static final String MESSAGE = "올바르지 않은 토큰입니다.";

    public InvalidTokenException(Exception exception) {
        super(MESSAGE, exception);
    }

    public InvalidTokenException() {
        super(MESSAGE);
    }
}
