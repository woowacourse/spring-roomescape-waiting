package roomescape.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("권한이 없는 접근입니다.");
    }

    public UnauthorizedException(final String message) {
        super(message);
    }

    public UnauthorizedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(final Throwable cause) {
        super(cause);
    }
}
