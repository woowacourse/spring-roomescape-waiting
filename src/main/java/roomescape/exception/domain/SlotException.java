package roomescape.exception.domain;

import roomescape.exception.RoomescapeException;
import roomescape.exception.code.SlotErrorCode;

public class SlotException extends RoomescapeException {

    public SlotException(SlotErrorCode slotErrorCode) {
        super(slotErrorCode);
    }
}
