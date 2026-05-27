package roomescape.exception.reservation;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class ReservationOwnerMismatchException extends BusinessException {

    public ReservationOwnerMismatchException() {
        super(ErrorType.RESERVATION_OWNER_MISMATCH);
    }
}
