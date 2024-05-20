package roomescape.exception;

public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super("인증 정보가 일치하지 않습니다.");
    }
}
