package roomescape.exception.member;

public class UnauthorizedEmailException extends UnauthorizedException {

    public UnauthorizedEmailException() {
        super("이메일이 존재하지 않습니다.");
    }
}
