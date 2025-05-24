package roomescape.reservation.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomEscapeException;

public class ThemeFieldRequiredException extends RoomEscapeException {
    public ThemeFieldRequiredException(String field) {
        super(HttpStatus.BAD_REQUEST, field + "은/는 필수 입력값입니다.");
    }
}
