package roomescape.exception.time;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class NotFoundTimeException extends RoomescapeException {
    public NotFoundTimeException() {
        super("존재하지 않는 시간입니다.", HttpStatus.NOT_FOUND);
    }
}
