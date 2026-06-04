package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class AuthenticationException extends BusinessException {

    public AuthenticationException() {
        super(ErrorType.LOGIN_FAILED);
    }
}
