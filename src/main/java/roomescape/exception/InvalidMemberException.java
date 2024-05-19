package roomescape.exception;

public class InvalidMemberException extends RuntimeException {
    public InvalidMemberException() {
        super();
    }

    public InvalidMemberException(final String message) {
        super(message);
    }

    public InvalidMemberException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InvalidMemberException(final Throwable cause) {
        super(cause);
    }
}
