package roomescape.reservation.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomEscapeException;

public class UsingThemeException extends RoomEscapeException {
    public UsingThemeException() {
        super(HttpStatus.BAD_REQUEST, "예약 되어있는 테마는 삭제할 수 없습니다.");
    }
}
