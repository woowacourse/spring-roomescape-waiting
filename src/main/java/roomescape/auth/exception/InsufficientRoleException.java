package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;

public class InsufficientRoleException extends BusinessException {

    public InsufficientRoleException() {
        super(AuthErrorType.INSUFFICIENT_ROLE);
    }
}
