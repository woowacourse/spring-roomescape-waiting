package roomescape.reservationTime.exception;

import roomescape.common.exception.BusinessException;

public class UsingTimeException extends BusinessException {
    public UsingTimeException() {
        super(TimeErrorCode.USING_TIME);
    }
}
