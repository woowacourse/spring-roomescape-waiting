package roomescape.exception.custom;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

public class InvalidInputException extends CustomException {

    public InvalidInputException() {
        super(ErrorCode.INVALID_INPUT);
    }

    public InvalidInputException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
