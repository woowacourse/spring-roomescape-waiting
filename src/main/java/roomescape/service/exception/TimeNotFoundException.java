package roomescape.service.exception;

import roomescape.exception.NotFoundException;

public class TimeNotFoundException extends NotFoundException {

    public TimeNotFoundException(String message) {
        super(message);
    }
}
