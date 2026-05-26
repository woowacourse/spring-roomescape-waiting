package roomescape.theme.domain.exception;

import roomescape.common.exception.NotFoundException;

public class ThemeNotFoundException extends NotFoundException {
    public ThemeNotFoundException(String message) {
        super(message);
    }
}
