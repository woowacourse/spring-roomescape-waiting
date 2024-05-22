package roomescape.exception;

public class AuthorizationExpiredException extends AuthorizationException {
    public AuthorizationExpiredException() {
        super("로그인이 만료되었습니다. 다시 로그인 해주세요.");
    }
}
