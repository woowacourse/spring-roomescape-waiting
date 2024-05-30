package roomescape.exception.reservation;

import org.springframework.http.HttpStatus;
import roomescape.exception.CustomException;

public class ReservationException extends CustomException {
    public ReservationException() {
        super("예약을 실패했습니다.", HttpStatus.BAD_REQUEST);
    }

    public ReservationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
