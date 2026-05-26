package roomescape.theme.application.exception;

import roomescape.common.exception.AlreadyInUseException;

public class ThemeInUseException extends AlreadyInUseException {
    public ThemeInUseException(String message) {
        super(message);
    }
}
