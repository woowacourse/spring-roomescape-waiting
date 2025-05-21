package roomescape.auth.exception;

import roomescape.common.exception.BusinessException;

public class TokenIsEmptyException extends BusinessException {
    public TokenIsEmptyException() {
        super(AuthErrorCode.TOKEN_IS_EMPTY);
    }
}
