package roomescape.theme.exception;

import roomescape.common.exception.BusinessException;

public class ThemeInUseException extends BusinessException {

    public ThemeInUseException() {
        super(ThemeErrorType.IN_USE);
    }
}
