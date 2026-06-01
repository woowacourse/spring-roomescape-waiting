package roomescape.reservationwait.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class ReservationWaitAlreadyExistsException extends BusinessException {

    public ReservationWaitAlreadyExistsException() {
        super(ErrorType.RESERVATION_WAIT_ALREADY_EXISTS);
    }
}
