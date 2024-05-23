package roomescape.exception.waiting;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class NotFoundWaitingException extends RoomescapeException {
    public NotFoundWaitingException() {
        super("존재하지 않는 예약 대기입니다.", HttpStatus.NOT_FOUND);
    }
}
