package roomescape.infrastructure.error.exception;

public class AuthInfoResolveException extends RuntimeException {

    public AuthInfoResolveException(String message) {
        super(message);
    }

    public AuthInfoResolveException(String message, Throwable cause) {
        super(message, cause);
    }
}
