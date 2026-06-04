package roomescape.reservationwait.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class PastReservationWaitNotAllowedException extends BusinessException {
    public PastReservationWaitNotAllowedException() {
        super(ErrorType.PAST_RESERVATION_WAIT_NOT_ALLOWED);
    }
}
