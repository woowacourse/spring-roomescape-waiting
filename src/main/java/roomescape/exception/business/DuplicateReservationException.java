package roomescape.exception.business;

import roomescape.exception.ErrorCode;

public class DuplicateReservationException extends BusinessException {

    public DuplicateReservationException() {
        super(ErrorCode.DUPLICATE_RESERVATION);
    }
}
