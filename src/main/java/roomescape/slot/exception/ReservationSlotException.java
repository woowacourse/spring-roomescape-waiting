package roomescape.slot.exception;

import roomescape.common.exception.ErrorInformation;
import roomescape.common.exception.RoomEscapeException;

public class ReservationSlotException extends RoomEscapeException {
    public ReservationSlotException(ErrorInformation errorInformation) {
        super(errorInformation);
    }
}
