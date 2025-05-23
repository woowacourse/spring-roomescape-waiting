package roomescape.waiting.exception;

import roomescape.common.exception.NotFoundException;

public class WaitingNotFoundException extends NotFoundException {

    public WaitingNotFoundException(final String message) {
        super(message);
    }
}
