package roomescape.time.exception.model;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.model.RoomEscapeException;

public class TimeNotFoundException extends RoomEscapeException {

    private static final String TIME_NOT_EXIST_MESSAGE = "해당하는 시간이 존재하지 않습니다.";

    public TimeNotFoundException() {
        super(TIME_NOT_EXIST_MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
