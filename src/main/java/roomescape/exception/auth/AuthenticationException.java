package roomescape.exception.auth;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class AuthenticationException extends BusinessException {

    public AuthenticationException() {
        super(ErrorType.LOGIN_FAILED);
    }
}
