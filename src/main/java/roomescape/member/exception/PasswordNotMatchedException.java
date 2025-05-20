package roomescape.member.exception;

import roomescape.common.exception.BusinessException;

public class PasswordNotMatchedException extends BusinessException {
    public PasswordNotMatchedException() {
        super(MemberErrorCode.PASSWORD_NOT_MATCHED);
    }
}
