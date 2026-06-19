package roomescape.reservation.application.exception;

import roomescape.common.exception.AlreadyChangeException;

public class ReservationAlreadyChangedException extends AlreadyChangeException {
    public ReservationAlreadyChangedException(String message) {
        super(message);
    }
}
