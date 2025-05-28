package roomescape.exception.custom;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

public class AuthenticatedException extends CustomException {

    public AuthenticatedException() {
        super(ErrorCode.NOT_AUTHENTICATED);
    }

    public AuthenticatedException(final String detail) {
        super(ErrorCode.NOT_AUTHENTICATED, detail);
    }
}
