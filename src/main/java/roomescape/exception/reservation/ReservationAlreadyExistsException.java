package roomescape.exception.reservation;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class ReservationAlreadyExistsException extends RoomescapeException {
    public ReservationAlreadyExistsException() {
        super(HttpStatus.BAD_REQUEST, "이미 예약이 존재합니다.");
    }
}
