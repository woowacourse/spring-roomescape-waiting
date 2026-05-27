package roomescape.exception.auth;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException() {
        super(ErrorType.INVALID_TOKEN);
    }
}
