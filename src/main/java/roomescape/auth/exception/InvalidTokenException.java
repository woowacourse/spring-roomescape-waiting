package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException() {
        super(AuthErrorCode.INVALID_TOKEN);
    }
}
