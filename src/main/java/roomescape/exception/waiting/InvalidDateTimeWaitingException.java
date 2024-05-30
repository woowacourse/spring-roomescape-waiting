package roomescape.exception.waiting;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class InvalidDateTimeWaitingException extends RoomescapeException {
    public InvalidDateTimeWaitingException() {
        super("지난간 날짜와 시간에 대한 예약 대기입니다.", HttpStatus.BAD_REQUEST);
    }
}
