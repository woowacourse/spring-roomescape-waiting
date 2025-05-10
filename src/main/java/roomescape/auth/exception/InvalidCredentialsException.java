package roomescape.auth.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("잘못된 이메일 혹은 비밀번호를 입력하였다.");
    }
}
