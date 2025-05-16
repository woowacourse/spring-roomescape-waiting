package roomescape.exception.reservationtime;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomEscapeException;

public class ReservationTimeFieldRequiredException extends RoomEscapeException {
    public ReservationTimeFieldRequiredException(String field) {
        super(HttpStatus.BAD_REQUEST, field + "은/는 필수 입력값입니다.");
    }
}
