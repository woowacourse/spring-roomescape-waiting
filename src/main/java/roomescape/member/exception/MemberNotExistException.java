package roomescape.member.exception;

public class MemberNotExistException extends RuntimeException {

    public MemberNotExistException() {
        super("멤버를 찾을 수 없다.");
    }
}
