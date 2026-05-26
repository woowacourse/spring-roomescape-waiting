package roomescape.exception;

public class AuthenticationException extends BusinessException {

    public AuthenticationException() {
        super(ErrorType.LOGIN_FAILED);
    }
}
