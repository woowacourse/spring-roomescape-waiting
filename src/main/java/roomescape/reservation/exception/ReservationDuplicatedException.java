package roomescape.reservation.exception;

import roomescape.common.exception.DuplicatedException;

public class ReservationDuplicatedException extends DuplicatedException {

    public ReservationDuplicatedException(final String message) {
        super(message);
    }
}
