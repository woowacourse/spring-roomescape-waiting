package roomescape.member.exception;

import roomescape.common.exception.BusinessException;

public class NameRequiredException extends BusinessException {
    public NameRequiredException() {
        super(MemberErrorCode.NAME_REQUIRED);
    }
}
