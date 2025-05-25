package roomescape.reservation.exception;

import roomescape.global.exception.InAlreadyException;

public class InAlreadyReservationException extends InAlreadyException {
    public InAlreadyReservationException(String message) {
        super(message);
    }
}
