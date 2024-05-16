package roomescape.exception;

public class AccessNotAllowException extends IllegalArgumentException {
    public AccessNotAllowException(final String message) {
        super(message);
    }
}

