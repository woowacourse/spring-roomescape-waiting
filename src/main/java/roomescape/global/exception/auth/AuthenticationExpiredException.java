package roomescape.global.exception.auth;

public class AuthenticationExpiredException extends AuthenticationException {

    public AuthenticationExpiredException(String message) {
        super(message);
    }

    public AuthenticationExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
