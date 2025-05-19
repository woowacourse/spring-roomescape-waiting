package roomescape.reservation.exception;

import roomescape.global.common.exception.ValidationException;

public class InvalidReservationException extends ValidationException {

    public InvalidReservationException(final String message) {
        super(message);
    }
}
