package roomescape.exception.reservationwait;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class SelfReservationWaitNotAllowedException extends BusinessException {

    public SelfReservationWaitNotAllowedException() {
        super(ErrorType.SELF_RESERVATION_WAIT_NOT_ALLOWED);
    }
}
