package roomescape.exception.auth;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class ExpiredTokenException extends BusinessException {

    public ExpiredTokenException() {
        super(ErrorType.EXPIRED_TOKEN);
    }
}
