package roomescape.application.support.exception;

public class LoginAuthException extends UnauthorizedException {

    public LoginAuthException(String message) {
        super(message);
    }

    public LoginAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
