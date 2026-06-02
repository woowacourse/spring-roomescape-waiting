package roomescape.exception.reservationwait;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class ReservationWaitAlreadyExistsException extends BusinessException {

    public ReservationWaitAlreadyExistsException() {
        super(ErrorType.RESERVATION_WAIT_ALREADY_EXISTS);
    }
}
