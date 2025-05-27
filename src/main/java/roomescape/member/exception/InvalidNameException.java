package roomescape.member.exception;

import roomescape.common.exception.BusinessException;

public class InvalidNameException extends BusinessException {
    public InvalidNameException() {
        super(MemberErrorCode.INVALID_NAME);
    }
}
