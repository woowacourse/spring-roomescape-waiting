package roomescape.reservation.exception;

import roomescape.common.exception.BusinessException;

public class ReservationAlreadyExistsException extends BusinessException {

    public ReservationAlreadyExistsException() {
        super(ReservationErrorType.ALREADY_EXISTS);
    }
}
