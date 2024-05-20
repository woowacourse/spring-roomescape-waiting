package roomescape.exception.theme;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class NotFoundThemeException extends RoomescapeException {
    public NotFoundThemeException() {
        super("존재하지 않는 테마입니다.", HttpStatus.NOT_FOUND);
    }
}
