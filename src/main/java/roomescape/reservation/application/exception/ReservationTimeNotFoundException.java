package roomescape.reservation.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomEscapeException;

public class ReservationTimeNotFoundException extends RoomEscapeException {
    public ReservationTimeNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "시간을 찾을 수 없습니다. id=" + id);
    }
}
