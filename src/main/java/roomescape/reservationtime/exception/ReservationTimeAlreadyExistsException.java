package roomescape.reservationtime.exception;

import roomescape.exception.AlreadyExistsException;

public class ReservationTimeAlreadyExistsException extends AlreadyExistsException {

    public ReservationTimeAlreadyExistsException(final String message) {
        super(message);
    }
}
