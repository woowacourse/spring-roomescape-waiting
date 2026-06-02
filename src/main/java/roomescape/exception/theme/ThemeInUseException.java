package roomescape.exception.theme;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class ThemeInUseException extends BusinessException {

    public ThemeInUseException() {
        super(ErrorType.THEME_IN_USE);
    }
}
