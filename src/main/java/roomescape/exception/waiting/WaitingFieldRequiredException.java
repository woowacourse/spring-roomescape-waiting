package roomescape.exception.waiting;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomEscapeException;

public class WaitingFieldRequiredException extends RoomEscapeException {

    public WaitingFieldRequiredException(String message) {
        super(HttpStatus.BAD_REQUEST, message+"는 필수 입력입니다.");
    }
}
