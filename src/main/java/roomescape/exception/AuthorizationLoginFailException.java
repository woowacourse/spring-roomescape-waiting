package roomescape.exception;

public class AuthorizationLoginFailException extends AuthorizationException {
    public AuthorizationLoginFailException() {
        super("로그인 정보가 잘못되었습니다.");
    }
}
