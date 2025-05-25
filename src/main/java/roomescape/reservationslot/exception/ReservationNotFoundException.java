package roomescape.reservationslot.exception;

import roomescape.common.exception.NotFoundException;

public class ReservationNotFoundException extends NotFoundException {

    public ReservationNotFoundException(final String message) {
        super(message);
    }
}
