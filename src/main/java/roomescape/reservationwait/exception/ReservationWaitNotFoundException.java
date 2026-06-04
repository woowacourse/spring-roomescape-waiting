package roomescape.reservationwait.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class ReservationWaitNotFoundException extends BusinessException {

    public ReservationWaitNotFoundException() {
        super(ErrorType.RESERVATION_WAIT_NOT_FOUND);
    }
}
