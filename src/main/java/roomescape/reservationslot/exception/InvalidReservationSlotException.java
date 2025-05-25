package roomescape.reservationslot.exception;

import roomescape.common.exception.ValidationException;

public class InvalidReservationSlotException extends ValidationException {

    public InvalidReservationSlotException(final String message) {
        super(message);
    }
}
