package roomescape.reservationTime.exception;

import roomescape.common.exception.BusinessException;

public class TimeNotFoundException extends BusinessException {
    public TimeNotFoundException() {
        super(TimeErrorCode.TIME_NOT_FOUND);
    }
}
