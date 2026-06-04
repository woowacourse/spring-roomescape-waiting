package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException() {
        super(ErrorType.INVALID_TOKEN);
    }
}
