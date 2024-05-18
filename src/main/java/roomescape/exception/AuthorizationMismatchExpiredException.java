package roomescape.exception;

public class AuthorizationMismatchExpiredException extends AuthorizationException {
    public AuthorizationMismatchExpiredException() {
        super("비밀번호가 일치하지 않습니다.");
    }
}
