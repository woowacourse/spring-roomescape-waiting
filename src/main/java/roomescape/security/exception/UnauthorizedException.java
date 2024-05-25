package roomescape.security.exception;

public class UnauthorizedException extends RuntimeException {

    private static final String MESSAGE = "로그인이 필요합니다.";

    public UnauthorizedException() {
        super(MESSAGE);
    }

    public UnauthorizedException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
