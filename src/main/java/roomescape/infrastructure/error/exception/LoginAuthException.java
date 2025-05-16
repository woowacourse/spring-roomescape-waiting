package roomescape.infrastructure.error.exception;

public class LoginAuthException extends UnauthorizedException {

    public LoginAuthException(String message) {
        super(message);
    }

    public LoginAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
