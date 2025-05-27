package roomescape.reservation.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomEscapeException;

public class NotReservationOwnerException extends RoomEscapeException {

    public NotReservationOwnerException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
