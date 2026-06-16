package roomescape.common.exception;

public class UpdateException extends RuntimeException {
    public UpdateException(String message) {
        super(message);
    }

    public UpdateException(String message, Exception e) {
        super(message, e);
    }
}
