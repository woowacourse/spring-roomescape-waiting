package roomescape.reservation.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomEscapeException;

public class ThemeNotFoundException extends RoomEscapeException {
    public ThemeNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "테마를 찾을 수 없습니다. id=" + id);
    }
}
