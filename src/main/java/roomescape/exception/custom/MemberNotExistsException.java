package roomescape.exception.custom;

public class MemberNotExistsException extends CustomException {

    public MemberNotExistsException() {
        super("해당 회원이 존재하지 않습니다.");
    }
}
