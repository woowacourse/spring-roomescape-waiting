package roomescape.exception;

public class TokenValidationFailureException extends BaseException {

    public TokenValidationFailureException() {
        this("토큰이 존재하지 않습니다.");
    }

    public TokenValidationFailureException(String detail) {
        super("인증에 실패했습니다.", detail);
    }
}
