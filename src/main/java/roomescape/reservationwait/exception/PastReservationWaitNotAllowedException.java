package roomescape.reservationwait.exception;

import roomescape.common.exception.BusinessException;

public class PastReservationWaitNotAllowedException extends BusinessException {
    public PastReservationWaitNotAllowedException() {
        super(ReservationWaitErrorType.PAST_NOT_ALLOWED);
    }
}
