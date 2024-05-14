package roomescape.exception;

public class DeleteNotAllowException extends IllegalArgumentException {
    private final String message;

    public DeleteNotAllowException(final String message) {
        super(message);
        this.message = message;
    }
}
