package roomescape.member.exception;

import roomescape.common.exception.BusinessException;

public class EmailRequiredException extends BusinessException {
    public EmailRequiredException() {
        super(MemberErrorCode.EMAIL_REQUIRED);
    }
}
