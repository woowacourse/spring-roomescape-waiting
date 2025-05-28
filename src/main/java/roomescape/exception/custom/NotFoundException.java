package roomescape.exception.custom;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

public class NotFoundException extends CustomException {

    public NotFoundException() {
        super(ErrorCode.NOT_FOUND);
    }

    public NotFoundException(final String detail) {
        super(ErrorCode.NOT_FOUND, detail);
    }
}
