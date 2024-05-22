package roomescape.exception;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(final String message) {
        super(message);
    }

    public AccessDeniedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
