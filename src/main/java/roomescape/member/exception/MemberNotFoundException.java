package roomescape.member.exception;

import roomescape.common.exception.BusinessException;

public class MemberNotFoundException extends BusinessException {
    public MemberNotFoundException() {
        super(MemberErrorCode.MEMBER_NOT_FOUND);
    }
}
