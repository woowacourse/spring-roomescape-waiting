package roomescape.reservationTime.exception;

import roomescape.common.exception.BusinessException;

public class ReservationTimeException extends BusinessException {
    public ReservationTimeException(String message) {
        super(message);
    }
}
