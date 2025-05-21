package roomescape.member.exception;

import roomescape.common.exception.BusinessException;

public class MemberRoleNotExistsException extends BusinessException {
    public MemberRoleNotExistsException() {
        super(MemberErrorCode.ROLE_NOT_FOUND);
    }
}
