package roomescape.reservation.domain.exception;

import roomescape.common.exception.IllegalDateTimeException;

public class IllegalReservationDateTimeException extends IllegalDateTimeException {
    public IllegalReservationDateTimeException(String message) {
        super(message);
    }
}
