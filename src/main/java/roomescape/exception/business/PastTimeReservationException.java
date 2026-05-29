package roomescape.exception.business;

import roomescape.exception.ErrorCode;

public class PastTimeReservationException extends BusinessException {

    public PastTimeReservationException() {
        super(ErrorCode.PAST_TIME_RESERVATION);
    }
}
