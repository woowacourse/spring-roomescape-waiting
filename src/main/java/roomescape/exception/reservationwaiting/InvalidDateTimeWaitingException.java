package roomescape.exception.reservationwaiting;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class InvalidDateTimeWaitingException extends RoomescapeException {
    public InvalidDateTimeWaitingException() {
        super("지나간 날짜와 시간에 대한 예약 대기입니다.", HttpStatus.BAD_REQUEST);
    }
}
