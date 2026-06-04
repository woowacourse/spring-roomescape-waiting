package roomescape.reservation.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class PastReservationCancelNotAllowedException extends BusinessException {

    public PastReservationCancelNotAllowedException() {
        super(ErrorType.PAST_RESERVATION_CANCEL_NOT_ALLOWED);
    }
}
