package roomescape.theme.exception;

import roomescape.common.exception.BusinessException;

public class ThemeNotFoundException extends BusinessException {
    public ThemeNotFoundException() {
        super(ThemeErrorCode.THEME_NOT_FOUND);
    }
}
