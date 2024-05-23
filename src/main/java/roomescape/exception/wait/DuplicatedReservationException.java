package roomescape.exception.wait;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class DuplicatedReservationException extends CustomException {
    public DuplicatedReservationException() {
        super("중복된 예약대기입니다.", HttpStatus.CONFLICT);
    }
}
