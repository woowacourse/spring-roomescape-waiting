package roomescape.reservation.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class ReservationNotFoundException extends BusinessException {

    public ReservationNotFoundException() {
        super(ErrorType.RESERVATION_NOT_FOUND);
    }
}
