package roomescape.application.support.exception;

public class UnauthorizedException extends CoreException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class LoginAuthException extends UnauthorizedException {

        public LoginAuthException(String message) {
            super(message);
        }

        public LoginAuthException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
