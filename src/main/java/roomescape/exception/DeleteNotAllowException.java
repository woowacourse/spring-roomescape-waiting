package roomescape.exception;

public class DeleteNotAllowException extends IllegalArgumentException {
    public DeleteNotAllowException(final String message) {
        super(message);
    }
}
