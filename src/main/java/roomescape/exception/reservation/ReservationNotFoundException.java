package roomescape.exception.reservation;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class ReservationNotFoundException extends BusinessException {

    public ReservationNotFoundException() {
        super(ErrorType.RESERVATION_NOT_FOUND);
    }
}
