package roomescape.exception.reservationwaiting;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class DuplicatedReservationWaitingException extends RoomescapeException {
    public DuplicatedReservationWaitingException() {
        super("해당 사용자의 해당 예약에 대한 대기가 이미 존재합니다.", HttpStatus.CONFLICT);
    }
}
