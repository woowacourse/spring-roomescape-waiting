package roomescape.service.exception;

import roomescape.exception.RoomescapeException;

public class ReservationNotFoundException extends RoomescapeException {

    public ReservationNotFoundException(final String message) {
        super(message);
    }
}
