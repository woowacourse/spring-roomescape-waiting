package roomescape.reservationwait.exception;

import roomescape.common.exception.BusinessException;

public class SelfReservationWaitNotAllowedException extends BusinessException {

    public SelfReservationWaitNotAllowedException() {
        super(ReservationWaitErrorType.SELF_NOT_ALLOWED);
    }
}
