package roomescape.reservationtime.exception;

import roomescape.common.exception.BusinessException;

public class ReservationTimeInUseException extends BusinessException {

    public ReservationTimeInUseException() {
        super(ReservationTimeErrorType.IN_USE);
    }
}
