package roomescape.global.exception.customException;

import roomescape.global.exception.ErrorCode;

public class BadRequestException extends RoomEscapeException {

    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
