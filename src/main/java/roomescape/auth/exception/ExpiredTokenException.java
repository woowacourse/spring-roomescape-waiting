package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class ExpiredTokenException extends BusinessException {

    public ExpiredTokenException() {
        super(ErrorType.EXPIRED_TOKEN);
    }
}
