package roomescape.service.exception;

import roomescape.domain.exception.NotFoundException;

public class ThemeNotFoundException extends NotFoundException {
    public ThemeNotFoundException(String message) {
        super(message);
    }
}
