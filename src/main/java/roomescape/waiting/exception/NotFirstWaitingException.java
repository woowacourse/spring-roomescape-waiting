package roomescape.waiting.exception;

import roomescape.common.exception.BusinessException;

public class NotFirstWaitingException extends BusinessException {
    public NotFirstWaitingException() {
        super(WaitingErrorCode.NOT_FIRST_WAITING);
    }
}
