package roomescape.reservation.exception;

import roomescape.common.exception.ErrorInformation;
import roomescape.common.exception.RoomEscapeException;

public class ReservationException extends RoomEscapeException {
    public ReservationException(ErrorInformation errorCode) {
        super(errorCode);
    }
}
