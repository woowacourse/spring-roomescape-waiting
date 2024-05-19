package roomescape.service.exception;

import roomescape.exception.RoomescapeException;

public class DuplicateReservationException extends RoomescapeException {

    public DuplicateReservationException(final String message) {
        super(message);
    }
}
