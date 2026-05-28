package roomescape.theme.application.exception;

import roomescape.common.exception.DuplicateException;

public class DuplicateThemeException extends DuplicateException {
    public DuplicateThemeException(String message) {
        super(message);
    }
}
