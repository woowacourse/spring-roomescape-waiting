package roomescape.waiting.exception;

import roomescape.common.exception.BusinessException;

public class TooManyWaitingException extends BusinessException {
    public TooManyWaitingException() {
        super(WaitingErrorCode.TOO_MANY_WAITING);
    }
}
