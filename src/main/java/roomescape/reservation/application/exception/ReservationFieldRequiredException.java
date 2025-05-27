package roomescape.reservation.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomEscapeException;

public class ReservationFieldRequiredException extends RoomEscapeException {
    public ReservationFieldRequiredException(String field) {
        super(HttpStatus.BAD_REQUEST, field + "은/는 필수 입력값입니다.");
    }
}
