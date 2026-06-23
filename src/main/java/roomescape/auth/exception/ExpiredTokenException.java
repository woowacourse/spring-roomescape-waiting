package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;

public class ExpiredTokenException extends BusinessException {

    public ExpiredTokenException() {
        super(AuthErrorType.EXPIRED_TOKEN);
    }
}
