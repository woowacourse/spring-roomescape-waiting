package roomescape.theme.exception;

import roomescape.common.exception.ErrorInformation;
import roomescape.common.exception.RoomEscapeException;

public class ThemeException extends RoomEscapeException {
    public ThemeException(ErrorInformation errorCode) {
        super(errorCode);
    }
}
