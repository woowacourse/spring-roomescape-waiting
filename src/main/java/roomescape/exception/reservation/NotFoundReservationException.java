package roomescape.exception.reservation;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class NotFoundReservationException extends CustomException {
    public NotFoundReservationException() {
        super("존재하지 않는 예약입니다.", HttpStatus.NOT_FOUND);
    }
}
