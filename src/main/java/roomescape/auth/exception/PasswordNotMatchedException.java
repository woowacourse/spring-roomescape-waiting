package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;

public class PasswordNotMatchedException extends BusinessException {
    public PasswordNotMatchedException() {
        super(AuthErrorCode.PASSWORD_NOT_MATCHED);
    }
}
