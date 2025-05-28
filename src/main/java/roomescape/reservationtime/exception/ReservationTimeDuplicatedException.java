package roomescape.reservationtime.exception;

import roomescape.common.exception.DuplicatedException;

public class ReservationTimeDuplicatedException extends DuplicatedException {

    public ReservationTimeDuplicatedException(final String message) {
        super(message);
    }
}
