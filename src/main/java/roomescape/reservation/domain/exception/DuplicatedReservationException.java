package roomescape.reservation.domain.exception;

import roomescape.common.exception.DuplicateException;

public class DuplicatedReservationException extends DuplicateException {
    public DuplicatedReservationException(String message) {
        super(message);
    }
}
