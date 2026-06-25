package roomescape.reservationwait.exception;

import roomescape.common.exception.BusinessException;

public class PendingReservationWaitNotAllowedException extends BusinessException {

    public PendingReservationWaitNotAllowedException() {
        super(ReservationWaitErrorType.PENDING_NOT_ALLOWED);
    }
}
