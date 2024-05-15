package roomescape.exception.member;

public class UnauthorizedPasswordException extends UnauthorizedException {

    public UnauthorizedPasswordException() {
        super("비밀번호가 올바르지 않습니다.");
    }
}
