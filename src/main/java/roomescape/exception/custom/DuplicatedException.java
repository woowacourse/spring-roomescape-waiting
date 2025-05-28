package roomescape.exception.custom;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

public class DuplicatedException extends CustomException {

    public DuplicatedException() {
        super(ErrorCode.DUPLICATED);
    }

    public DuplicatedException(final String detail) {
        super(ErrorCode.DUPLICATED, detail);
    }
}
