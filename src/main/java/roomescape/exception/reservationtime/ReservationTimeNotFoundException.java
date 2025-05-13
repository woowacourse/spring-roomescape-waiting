package roomescape.exception.reservationtime;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class ReservationTimeNotFoundException extends RoomescapeException {
    public ReservationTimeNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "시간을 찾을 수 없습니다. id=" + id);
    }
}
