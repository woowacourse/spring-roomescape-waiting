package roomescape.exception.reservation;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class ReservationInPastException extends RoomescapeException {
    public ReservationInPastException() {
        super(HttpStatus.BAD_REQUEST, "이미 지난 시간에는 예약을 할 수 없습니다.");
    }
}
