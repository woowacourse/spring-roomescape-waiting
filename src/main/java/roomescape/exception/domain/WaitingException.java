package roomescape.exception.domain;

import roomescape.exception.RoomescapeException;
import roomescape.exception.code.WaitingErrorCode;

public class WaitingException extends RoomescapeException {

    public WaitingException(WaitingErrorCode waitingErrorCode) {
        super(waitingErrorCode);
    }
}
