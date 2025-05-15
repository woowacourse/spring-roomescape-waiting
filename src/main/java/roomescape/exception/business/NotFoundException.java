package roomescape.exception.business;

import roomescape.exception.ErrorCode;
import roomescape.exception.RootBusinessException;

public class NotFoundException extends RootBusinessException {

    public NotFoundException(final ErrorCode code) {
        super(code);
    }
}
