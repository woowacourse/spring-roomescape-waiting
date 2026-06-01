package roomescape.reservationtime.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class ReservationTimeInUseException extends BusinessException {

    public ReservationTimeInUseException() {
        super(ErrorType.RESERVATION_TIME_IN_USE);
    }
}
