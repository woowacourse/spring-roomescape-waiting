package roomescape.exception.reservation;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomEscapeException;

public class ReservationNotFoundException extends RoomEscapeException {
    public ReservationNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다. id=" + id);
    }
}
