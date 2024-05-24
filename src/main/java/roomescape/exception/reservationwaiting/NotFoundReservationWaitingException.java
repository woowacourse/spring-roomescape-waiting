package roomescape.exception.reservationwaiting;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class NotFoundReservationWaitingException extends RoomescapeException {
    public NotFoundReservationWaitingException() {
        super("존재하지 않는 예약 대기입니다.", HttpStatus.NOT_FOUND);
    }
}
