package roomescape.exception.custom.reason.member;

import roomescape.exception.custom.status.ConflictException;

public class MemberEmailConflictException extends ConflictException {

    public MemberEmailConflictException() {
        super("이미 계정이 존재하는 이메일입니다.");
    }
}
