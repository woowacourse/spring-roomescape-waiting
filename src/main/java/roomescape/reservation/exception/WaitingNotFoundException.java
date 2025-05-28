package roomescape.reservation.exception;

import roomescape.global.common.exception.NotFoundException;

public class WaitingNotFoundException extends NotFoundException {

    public WaitingNotFoundException(final String message) {
        super(message);
    }
}
