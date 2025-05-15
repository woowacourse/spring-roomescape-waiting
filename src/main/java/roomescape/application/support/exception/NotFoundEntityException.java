package roomescape.application.support.exception;

public class NotFoundEntityException extends CoreException {

    public NotFoundEntityException(String message) {
        super(message);
    }

    public NotFoundEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
