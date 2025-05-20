package roomescape.member.exception;

import roomescape.common.exception.BusinessException;

public class PasswordRequiredException extends BusinessException {
    public PasswordRequiredException() {
        super(MemberErrorCode.PASSWORD_REQUIRED);
    }
}
