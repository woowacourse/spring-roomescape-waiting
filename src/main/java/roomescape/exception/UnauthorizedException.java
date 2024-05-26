package roomescape.exception;

public class UnauthorizedException extends ApplicationException {

    private static final String MESSAGE = "로그인이 필요합니다.";

    public UnauthorizedException() {
        super(MESSAGE);
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
