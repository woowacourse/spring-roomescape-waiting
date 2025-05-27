package roomescape.reservation.exception;

import roomescape.common.exception.AlreadyExistsException;

public class ReservationAlreadyExistsException extends AlreadyExistsException {

    public ReservationAlreadyExistsException(final String message) {
        super(message);
    }
}
