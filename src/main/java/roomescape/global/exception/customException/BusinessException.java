package roomescape.global.exception.customException;

import roomescape.global.exception.ErrorCode;

public class BusinessException extends RoomEscapeException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}
