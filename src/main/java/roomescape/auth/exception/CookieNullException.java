package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;

public class CookieNullException extends BusinessException {
    public CookieNullException() {
        super(AuthErrorCode.COOKIE_IS_NULL);
    }
}
