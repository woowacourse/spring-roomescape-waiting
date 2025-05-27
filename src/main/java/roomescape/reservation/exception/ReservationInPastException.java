package roomescape.reservation.exception;

import roomescape.common.exception.BusinessException;

public class ReservationInPastException extends BusinessException {
    public ReservationInPastException() {
        super(ReservationErrorCode.RESERVATION_IN_PAST);
    }
}
