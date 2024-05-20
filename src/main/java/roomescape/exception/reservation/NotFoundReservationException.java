package roomescape.exception.reservation;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class NotFoundReservationException extends RoomescapeException {
    public NotFoundReservationException() {
        super("존재하지 않는 예약입니다.", HttpStatus.NOT_FOUND);
    }
}
