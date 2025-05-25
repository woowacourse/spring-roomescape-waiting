package roomescape.reservation.exception;

import roomescape.common.exception.NotFoundException;

public class SlotReservationNotFoundException extends NotFoundException {

    public SlotReservationNotFoundException(final String message) {
        super(message);
    }
}
