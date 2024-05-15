package roomescape.service.exception;

import roomescape.exception.BadRequestException;

public class TimeUsingException extends BadRequestException {

    public TimeUsingException(String message) {
        super(message);
    }
}
