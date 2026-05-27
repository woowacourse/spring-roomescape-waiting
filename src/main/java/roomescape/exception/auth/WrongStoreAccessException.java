package roomescape.exception.auth;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class WrongStoreAccessException extends BusinessException {

    public WrongStoreAccessException() {
        super(ErrorType.WRONG_STORE_ACCESS);
    }
}
