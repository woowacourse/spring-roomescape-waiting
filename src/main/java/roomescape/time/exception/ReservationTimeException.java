package roomescape.time.exception;

import roomescape.common.exception.ErrorInformation;
import roomescape.common.exception.RoomEscapeException;

public class ReservationTimeException extends RoomEscapeException {
    public ReservationTimeException(ErrorInformation errorCode) {
        super(errorCode);
    }
}
