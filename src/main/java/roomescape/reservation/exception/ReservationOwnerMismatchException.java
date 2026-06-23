package roomescape.reservation.exception;

import roomescape.common.exception.BusinessException;

public class ReservationOwnerMismatchException extends BusinessException {

    public ReservationOwnerMismatchException() {
        super(ReservationErrorType.OWNER_MISMATCH);
    }
}
