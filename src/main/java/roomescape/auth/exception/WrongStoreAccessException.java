package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class WrongStoreAccessException extends BusinessException {

    public WrongStoreAccessException() {
        super(ErrorType.WRONG_STORE_ACCESS);
    }
}
