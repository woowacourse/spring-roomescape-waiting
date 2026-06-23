package roomescape.reservationwait.exception;

import roomescape.common.exception.BusinessException;

public class ReservationWaitNotFoundException extends BusinessException {

    public ReservationWaitNotFoundException() {
        super(ReservationWaitErrorType.NOT_FOUND);
    }
}
