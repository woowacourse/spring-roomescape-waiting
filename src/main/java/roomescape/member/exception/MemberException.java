package roomescape.member.exception;

import roomescape.common.exception.ErrorInformation;
import roomescape.common.exception.RoomEscapeException;

public class MemberException extends RoomEscapeException {
    public MemberException(ErrorInformation errorInformation) {
        super(errorInformation);
    }
}
