package roomescape.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("권한이 없는 접근입니다.");
    }

    public ForbiddenException(final String message) {
        super(message);
    }

    public ForbiddenException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ForbiddenException(final Throwable cause) {
        super(cause);
    }
}
