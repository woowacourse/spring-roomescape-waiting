package roomescape.exception.business;

import roomescape.exception.ErrorCode;

public class PastTimeCancelException extends BusinessException {

    public PastTimeCancelException() {
        super(ErrorCode.PAST_RESERVATION_CANCEL);
    }
}
