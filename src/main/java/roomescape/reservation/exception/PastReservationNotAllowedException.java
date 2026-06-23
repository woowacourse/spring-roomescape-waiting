package roomescape.reservation.exception;

import roomescape.common.exception.BusinessException;

public class PastReservationNotAllowedException extends BusinessException {

    public PastReservationNotAllowedException() {
        super(ReservationErrorType.PAST_NOT_ALLOWED);
    }
}
