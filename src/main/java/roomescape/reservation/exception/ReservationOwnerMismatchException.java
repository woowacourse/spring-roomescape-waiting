package roomescape.reservation.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class ReservationOwnerMismatchException extends BusinessException {

    public ReservationOwnerMismatchException() {
        super(ErrorType.RESERVATION_OWNER_MISMATCH);
    }
}
