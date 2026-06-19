package roomescape.reservation.infra.exception;

import roomescape.common.exception.AlreadyCancelledException;

public class ReservationAlreadyCancelledException extends AlreadyCancelledException {
    public ReservationAlreadyCancelledException(String message) {
        super(message);
    }
}
