package roomescape.service.exception;

import roomescape.exception.BadRequestException;

public class DateTimePassedException extends BadRequestException {

    public DateTimePassedException(String message) {
        super(message);
    }
}
