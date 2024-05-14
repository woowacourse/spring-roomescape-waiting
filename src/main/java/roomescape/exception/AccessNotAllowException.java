package roomescape.exception;

public class AccessNotAllowException extends IllegalArgumentException {
    private final String message;

    public AccessNotAllowException(final String message) {
        super(message);
        this.message = message;
    }
}

