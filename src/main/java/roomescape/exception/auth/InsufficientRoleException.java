package roomescape.exception.auth;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class InsufficientRoleException extends BusinessException {

    public InsufficientRoleException() {
        super(ErrorType.INSUFFICIENT_ROLE);
    }
}
