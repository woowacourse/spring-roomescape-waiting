package roomescape.exception.reservationwait;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class PastReservationWaitNotAllowedException extends BusinessException {
    public PastReservationWaitNotAllowedException() {
        super(ErrorType.PAST_RESERVATION_WAIT_NOT_ALLOWED);
    }
}
