package roomescape.reservation.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomEscapeException;

public class ReservationNotFoundException extends RoomEscapeException {
    public ReservationNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다. id=" + id);
    }
}
