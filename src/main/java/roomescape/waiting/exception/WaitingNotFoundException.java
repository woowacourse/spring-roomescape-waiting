package roomescape.waiting.exception;

import roomescape.common.exception.BusinessException;

public class WaitingNotFoundException extends BusinessException {
    public WaitingNotFoundException() {
        super(WaitingErrorCode.WAITING_NOT_FOUND);
    }
}
