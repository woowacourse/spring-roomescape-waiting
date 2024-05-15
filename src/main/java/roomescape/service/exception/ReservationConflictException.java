package roomescape.service.exception;

import roomescape.exception.ConflictException;

public class ReservationConflictException extends ConflictException {

    public ReservationConflictException(String message) {
        super(message);
    }
}
