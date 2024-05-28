package roomescape.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("권한이 없습니다. 관리자에게 문의해주세요.");
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
