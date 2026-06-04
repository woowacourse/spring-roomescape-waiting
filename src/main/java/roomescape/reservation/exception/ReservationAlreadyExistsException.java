package roomescape.reservation.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class ReservationAlreadyExistsException extends BusinessException {

    public ReservationAlreadyExistsException() {
        super(ErrorType.RESERVATION_ALREADY_EXISTS);
    }
}
