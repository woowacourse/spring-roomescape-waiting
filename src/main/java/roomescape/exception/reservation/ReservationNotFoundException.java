package roomescape.exception.reservation;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class ReservationNotFoundException extends RoomescapeException {
    public ReservationNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다. id=" + id);
    }
}
