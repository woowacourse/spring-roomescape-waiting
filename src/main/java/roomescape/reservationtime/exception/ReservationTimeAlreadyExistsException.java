package roomescape.reservationtime.exception;

import roomescape.common.exception.AlreadyExistsException;

public class ReservationTimeAlreadyExistsException extends AlreadyExistsException {

    public ReservationTimeAlreadyExistsException(final String message) {
        super(message);
    }
}
