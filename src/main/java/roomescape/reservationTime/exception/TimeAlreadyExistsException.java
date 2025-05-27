package roomescape.reservationTime.exception;

import roomescape.common.exception.BusinessException;

public class TimeAlreadyExistsException extends BusinessException {
    public TimeAlreadyExistsException() {
        super(TimeErrorCode.TIME_ALREADY_EXISTS);
    }
}
