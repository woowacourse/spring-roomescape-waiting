package roomescape.reservationtime.exception;

import roomescape.exception.NotFoundException;

public class ReservationTimeNotFoundException extends NotFoundException {

    public ReservationTimeNotFoundException(final String message) {
        super(message);
    }
}
