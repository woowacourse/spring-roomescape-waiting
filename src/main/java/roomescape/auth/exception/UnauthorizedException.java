package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException() {
        super(ErrorType.AUTHENTICATION_REQUIRED);
    }
}
