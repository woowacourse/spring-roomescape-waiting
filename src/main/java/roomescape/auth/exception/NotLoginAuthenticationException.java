package roomescape.auth.exception;

public class NotLoginAuthenticationException extends AuthenticationException {
    private static final String ERROR_MESSAGE = "인증이 되지 않은 유저입니다.";

    public NotLoginAuthenticationException() {
        super(ERROR_MESSAGE);
    }
}
