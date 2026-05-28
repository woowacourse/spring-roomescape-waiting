package roomescape.reservation.domain.exception;

import roomescape.common.exception.UnauthorizedException;

public class UnauthorizedReservationChangeException extends UnauthorizedException {
    public UnauthorizedReservationChangeException(String message) {
        super(message);
    }
}
