package roomescape.common.auth.exception;

import roomescape.common.exception.ErrorInformation;
import roomescape.common.exception.RoomEscapeException;

public class AuthException extends RoomEscapeException {

    public AuthException(ErrorInformation errorInformation) {
        super(errorInformation);
    }
}
