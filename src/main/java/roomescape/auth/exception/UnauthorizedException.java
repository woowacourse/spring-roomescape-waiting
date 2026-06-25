package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException() {
        super(AuthErrorType.AUTHENTICATION_REQUIRED);
    }
}
