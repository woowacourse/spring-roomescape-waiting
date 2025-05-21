package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;

public class AccessForbiddenException extends BusinessException {

    public AccessForbiddenException() {
        super(AuthErrorCode.ACCESS_FORBIDDEN);
    }
}
