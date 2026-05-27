package roomescape.exception.auth;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException() {
        super(ErrorType.AUTHENTICATION_REQUIRED);
    }
}
