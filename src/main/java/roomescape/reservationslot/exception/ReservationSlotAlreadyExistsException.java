package roomescape.reservationslot.exception;

import roomescape.common.exception.AlreadyExistsException;

public class ReservationSlotAlreadyExistsException extends AlreadyExistsException {

    public ReservationSlotAlreadyExistsException(final String message) {
        super(message);
    }
}
