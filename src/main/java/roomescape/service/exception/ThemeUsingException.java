package roomescape.service.exception;

import roomescape.exception.BadRequestException;

public class ThemeUsingException extends BadRequestException {

    public ThemeUsingException(String message) {
        super(message);
    }
}
