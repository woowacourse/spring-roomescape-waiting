package roomescape.exception.waiting;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class DuplicatedWaitingException extends RoomescapeException {
    public DuplicatedWaitingException() {
        super("해당 테마의 해당 시간대에 이미 예약 대기가 존재합니다.", HttpStatus.CONFLICT);
    }
}
