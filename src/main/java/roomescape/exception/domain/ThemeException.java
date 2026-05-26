package roomescape.exception.domain;


import roomescape.exception.RoomescapeException;
import roomescape.exception.code.ThemeErrorCode;

public class ThemeException extends RoomescapeException {

    public ThemeException(ThemeErrorCode themeErrorCode) {
        super(themeErrorCode);
    }
}
