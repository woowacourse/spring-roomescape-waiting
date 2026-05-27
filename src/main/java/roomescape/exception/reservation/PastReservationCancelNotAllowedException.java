package roomescape.exception.reservation;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class PastReservationCancelNotAllowedException extends BusinessException {

    public PastReservationCancelNotAllowedException() {
        super(ErrorType.PAST_RESERVATION_CANCEL_NOT_ALLOWED);
    }
}
