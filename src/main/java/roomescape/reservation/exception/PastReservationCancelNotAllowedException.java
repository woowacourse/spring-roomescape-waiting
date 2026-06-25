package roomescape.reservation.exception;

import roomescape.common.exception.BusinessException;

public class PastReservationCancelNotAllowedException extends BusinessException {

    public PastReservationCancelNotAllowedException() {
        super(ReservationErrorType.PAST_CANCEL_NOT_ALLOWED);
    }
}
