package roomescape.exception.reservation;

import org.springframework.http.HttpStatus;
import roomescape.exception.CustomException;

public class CancelReservationException extends CustomException {
    public CancelReservationException() {
        super("예약을 취소할 수 없습니다.", HttpStatus.BAD_REQUEST);
    }
}
