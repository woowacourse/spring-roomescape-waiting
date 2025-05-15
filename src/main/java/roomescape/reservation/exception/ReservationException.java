package roomescape.reservation.exception;

import roomescape.common.exception.BusinessException;

public class ReservationException extends BusinessException {
    public ReservationException(String message) {
        super(message);
    }
}
