package roomescape.date.exception;

import roomescape.common.exception.ErrorInformation;
import roomescape.common.exception.RoomEscapeException;

public class ReservationDateException extends RoomEscapeException {
    public ReservationDateException(ErrorInformation errorCode) {
        super(errorCode);
    }
}
