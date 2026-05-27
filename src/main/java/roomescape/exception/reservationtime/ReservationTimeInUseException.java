package roomescape.exception.reservationtime;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class ReservationTimeInUseException extends BusinessException {

    public ReservationTimeInUseException() {
        super(ErrorType.RESERVATION_TIME_IN_USE);
    }
}
