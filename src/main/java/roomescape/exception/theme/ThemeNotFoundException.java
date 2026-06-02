package roomescape.exception.theme;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class ThemeNotFoundException extends BusinessException {

    public ThemeNotFoundException() {
        super(ErrorType.THEME_NOT_FOUND);
    }
}
