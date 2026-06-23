package roomescape.reservationwait.exception;

import roomescape.common.exception.BusinessException;

public class ReservationWaitAlreadyExistsException extends BusinessException {

    public ReservationWaitAlreadyExistsException() {
        super(ReservationWaitErrorType.ALREADY_EXISTS);
    }
}
