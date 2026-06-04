package roomescape.reservationtime.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class ReservationTimeNotFoundException extends BusinessException {

    public ReservationTimeNotFoundException() {
        super(ErrorType.RESERVATION_TIME_NOT_FOUND);
    }
}
