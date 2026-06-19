package roomescape.common.exception;

public class ExternalSystemException extends RuntimeException {
    public ExternalSystemException(String message) {
        super(message);
    }

    public ExternalSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
