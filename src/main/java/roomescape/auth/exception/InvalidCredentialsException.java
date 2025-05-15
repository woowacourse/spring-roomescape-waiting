package roomescape.auth.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("이메일 혹은 비밀번호가 잘못되었습니다.");
    }
}
