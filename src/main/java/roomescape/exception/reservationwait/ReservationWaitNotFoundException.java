package roomescape.exception.reservationwait;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class ReservationWaitNotFoundException extends BusinessException {

    public ReservationWaitNotFoundException() {
        super(ErrorType.RESERVATION_WAIT_NOT_FOUND);
    }
}
