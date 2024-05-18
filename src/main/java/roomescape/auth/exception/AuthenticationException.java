package roomescape.auth.exception;

public abstract class AuthenticationException extends RuntimeException {
    protected AuthenticationException(String message) {
        super(message);
    }
}
