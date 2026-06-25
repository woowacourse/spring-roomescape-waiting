package roomescape.reservation.exception;

import roomescape.common.exception.BusinessException;

public class ReservationNotFoundException extends BusinessException {

    public ReservationNotFoundException() {
        super(ReservationErrorType.NOT_FOUND);
    }
}
