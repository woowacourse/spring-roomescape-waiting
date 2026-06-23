package roomescape.reservationwait.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class PendingReservationWaitNotAllowedException extends BusinessException {

    public PendingReservationWaitNotAllowedException() {
        super(ErrorType.PENDING_RESERVATION_WAIT_NOT_ALLOWED);
    }
}
