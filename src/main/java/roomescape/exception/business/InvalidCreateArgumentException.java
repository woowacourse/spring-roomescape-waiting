package roomescape.exception.business;

import roomescape.exception.ErrorCode;
import roomescape.exception.RootBusinessException;

public class InvalidCreateArgumentException extends RootBusinessException {

    public InvalidCreateArgumentException(final ErrorCode code) {
        super(code);
    }

    public InvalidCreateArgumentException(final ErrorCode code, final Object... args) {
        super(code, args);
    }
}
