package roomescape.exception.reservation;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class DuplicatedReservationException extends CustomException {
    public DuplicatedReservationException() {
        super("중복된 예약입니다.", HttpStatus.CONFLICT);
    }
}
