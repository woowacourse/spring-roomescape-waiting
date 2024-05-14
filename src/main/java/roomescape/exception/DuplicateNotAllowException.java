package roomescape.exception;

public class DuplicateNotAllowException extends IllegalArgumentException {
    private final String message;

    public DuplicateNotAllowException(final String message) {
        super(message);
        this.message = message;
    }
}
