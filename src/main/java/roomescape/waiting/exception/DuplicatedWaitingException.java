package roomescape.waiting.exception;

import roomescape.common.exception.BusinessException;

public class DuplicatedWaitingException extends BusinessException {
    public DuplicatedWaitingException() {
        super(WaitingErrorCode.DUPLICATED_WAITING);
    }
}
