package roomescape.time.application.exception;

import roomescape.common.exception.AlreadyInUseException;

public class ReservationTimeInUseException extends AlreadyInUseException {
    public ReservationTimeInUseException(String message) {
        super(message);
    }
}
