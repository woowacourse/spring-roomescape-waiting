package roomescape.reservationslot.exception;

import roomescape.common.exception.DuplicatedException;

public class ReservationSlotDuplicatedException extends DuplicatedException {

    public ReservationSlotDuplicatedException(final String message) {
        super(message);
    }
}
