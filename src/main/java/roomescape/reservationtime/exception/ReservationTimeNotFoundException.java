package roomescape.reservationtime.exception;

import roomescape.common.exception.BusinessException;

public class ReservationTimeNotFoundException extends BusinessException {

    public ReservationTimeNotFoundException() {
        super(ReservationTimeErrorType.NOT_FOUND);
    }
}
