package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;

public class WrongStoreAccessException extends BusinessException {

    public WrongStoreAccessException() {
        super(AuthErrorType.WRONG_STORE_ACCESS);
    }
}
