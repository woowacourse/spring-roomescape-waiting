package roomescape.reservationslot.exception;

import roomescape.common.exception.NotFoundException;

public class ReservationSlotNotFoundException extends NotFoundException {

    public ReservationSlotNotFoundException(final String message) {
        super(message);
    }
}
