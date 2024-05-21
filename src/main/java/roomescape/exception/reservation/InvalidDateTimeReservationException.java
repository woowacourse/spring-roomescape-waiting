package roomescape.exception.reservation;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class InvalidDateTimeReservationException extends RoomescapeException {
    public InvalidDateTimeReservationException() {
        super("지나간 날짜와 시간에 대한 예약입니다.", HttpStatus.BAD_REQUEST);
    }
}
