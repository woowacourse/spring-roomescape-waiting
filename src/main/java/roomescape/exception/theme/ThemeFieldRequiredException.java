package roomescape.exception.theme;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class ThemeFieldRequiredException extends RoomescapeException {
    public ThemeFieldRequiredException(String field) {
        super(HttpStatus.BAD_REQUEST, field + "은/는 필수 입력값입니다.");
    }
}
