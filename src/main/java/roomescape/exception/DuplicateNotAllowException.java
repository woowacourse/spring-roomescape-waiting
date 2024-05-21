package roomescape.exception;

public class DuplicateNotAllowException extends IllegalArgumentException {
    public DuplicateNotAllowException(final String message) {
        super(message);
    }
}
