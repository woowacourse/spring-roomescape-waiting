package roomescape.exception.waiting;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomEscapeException;

public class WaitingAlreadyExistException extends RoomEscapeException {

    public WaitingAlreadyExistException() {
        super(HttpStatus.BAD_REQUEST, "이미 예약 대기가 되어있습니다.");
    }
}
