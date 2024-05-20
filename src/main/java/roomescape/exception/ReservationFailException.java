package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ReservationFailException extends CustomException {
    public ReservationFailException(final String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
