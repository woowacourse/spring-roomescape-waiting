package roomescape.theme.exception;

import roomescape.common.exception.BusinessException;

public class UsingThemeException extends BusinessException {
    public UsingThemeException() {
        super(ThemeErrorCode.USING_THEME);
    }
}
