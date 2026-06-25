package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;

public class AuthenticationException extends BusinessException {

    public AuthenticationException() {
        super(AuthErrorType.LOGIN_FAILED);
    }
}
