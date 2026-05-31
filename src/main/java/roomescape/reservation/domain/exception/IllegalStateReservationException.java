package roomescape.reservation.domain.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.CustomException;

public class IllegalStateReservationException extends CustomException {

    public IllegalStateReservationException(final String message) {
        super("ILLEGAL_RESERVATION_STATE", HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
