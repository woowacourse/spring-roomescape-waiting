package roomescape.member.exception;

import roomescape.common.exception.BusinessException;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException() {
        super(MemberErrorCode.EMAIL_ALREADY_EXISTS);
    }
}
