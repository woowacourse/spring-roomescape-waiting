package roomescape.common.exception;

public class LoginFailException extends RuntimeException {

    public LoginFailException(final String message) {
        super(message);
    }
}
