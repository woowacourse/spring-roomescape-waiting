package roomescape.reservationwait.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class SelfReservationWaitNotAllowedException extends BusinessException {

    public SelfReservationWaitNotAllowedException() {
        super(ErrorType.SELF_RESERVATION_WAIT_NOT_ALLOWED);
    }
}
