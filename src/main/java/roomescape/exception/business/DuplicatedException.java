package roomescape.exception.business;

import roomescape.exception.ErrorCode;
import roomescape.exception.RootBusinessException;

public class DuplicatedException extends RootBusinessException {

    public DuplicatedException(final ErrorCode code) {
        super(code);
    }
}
