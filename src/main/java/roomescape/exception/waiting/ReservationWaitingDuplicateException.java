package roomescape.exception.waiting;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomEscapeException;

public class ReservationWaitingDuplicateException extends RoomEscapeException {

    public ReservationWaitingDuplicateException() {
        super(HttpStatus.BAD_REQUEST, "이미 예약되어 있기에 예약 대기를 할 수 없습니다.");
    }
}
