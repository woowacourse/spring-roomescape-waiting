package roomescape.reservationtime.exception;

import roomescape.common.exception.InUseException;

public class ReservationTimeInUseException extends InUseException {

    public ReservationTimeInUseException(final String message) {
        super(message);
    }
}
