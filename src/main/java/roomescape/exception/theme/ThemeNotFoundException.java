package roomescape.exception.theme;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class ThemeNotFoundException extends RoomescapeException {
    public ThemeNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "테마를 찾을 수 없습니다. id=" + id);
    }
}
