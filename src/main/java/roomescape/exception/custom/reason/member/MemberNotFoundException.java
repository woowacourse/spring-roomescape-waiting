package roomescape.exception.custom.reason.member;

import roomescape.exception.custom.status.NotFoundException;

public class MemberNotFoundException extends NotFoundException {
    public MemberNotFoundException() {
        super("멤버가 존재하지 않습니다.");
    }
}
