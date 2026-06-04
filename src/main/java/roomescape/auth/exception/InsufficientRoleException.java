package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class InsufficientRoleException extends BusinessException {

    public InsufficientRoleException() {
        super(ErrorType.INSUFFICIENT_ROLE);
    }
}
