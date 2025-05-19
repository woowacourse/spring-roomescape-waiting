package roomescape.reservation.exception;

import roomescape.global.common.exception.AlreadyExistsException;

public class ReservationAlreadyExistsException extends AlreadyExistsException {

    public ReservationAlreadyExistsException(final String message) {
        super(message);
    }
}
