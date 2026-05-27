package roomescape.exception.reservation;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class ReservationAlreadyExistsException extends BusinessException {

    public ReservationAlreadyExistsException() {
        super(ErrorType.RESERVATION_ALREADY_EXISTS);
    }
}
