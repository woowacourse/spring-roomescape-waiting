package roomescape.exception.reservationtime;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class ReservationTimeNotFoundException extends BusinessException {

    public ReservationTimeNotFoundException() {
        super(ErrorType.RESERVATION_TIME_NOT_FOUND);
    }
}
