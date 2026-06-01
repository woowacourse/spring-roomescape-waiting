package roomescape.theme.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class ThemeInUseException extends BusinessException {

    public ThemeInUseException() {
        super(ErrorType.THEME_IN_USE);
    }
}
