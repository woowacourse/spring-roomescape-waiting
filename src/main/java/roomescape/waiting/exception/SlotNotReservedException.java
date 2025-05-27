package roomescape.waiting.exception;

import roomescape.common.exception.BusinessException;

public class SlotNotReservedException extends BusinessException {
    public SlotNotReservedException() {
        super(WaitingErrorCode.SLOT_NOT_RESERVED);
    }
}
