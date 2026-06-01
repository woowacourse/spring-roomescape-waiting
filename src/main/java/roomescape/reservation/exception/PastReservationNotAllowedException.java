package roomescape.reservation.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class PastReservationNotAllowedException extends BusinessException {

    public PastReservationNotAllowedException() {
        super(ErrorType.PAST_RESERVATION_NOT_ALLOWED);
    }
}
