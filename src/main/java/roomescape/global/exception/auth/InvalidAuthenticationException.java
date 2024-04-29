package roomescape.global.exception.auth;

public class InvalidAuthenticationException extends AuthenticationException {

    public InvalidAuthenticationException(String message) {
        super(message);
    }

    public InvalidAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
