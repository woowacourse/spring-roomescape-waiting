package roomescape.member.exception;

import roomescape.common.exception.BusinessException;

public class InvalidEmailException extends BusinessException {
    public InvalidEmailException() {
        super(MemberErrorCode.INVALID_EMAIL);
    }
}
