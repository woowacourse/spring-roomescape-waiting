package roomescape.exception.reservation;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class PastReservationNotAllowedException extends BusinessException {

    public PastReservationNotAllowedException() {
        super(ErrorType.PAST_RESERVATION_NOT_ALLOWED);
    }
}
