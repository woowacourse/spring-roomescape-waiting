package roomescape.exception.time;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class DuplicatedTimeException extends RoomescapeException {
    public DuplicatedTimeException() {
        super("해당 예약 시간이 이미 존재합니다.", HttpStatus.CONFLICT);
    }
}
