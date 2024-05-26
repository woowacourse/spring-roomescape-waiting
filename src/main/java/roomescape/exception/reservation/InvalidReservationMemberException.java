package roomescape.exception.reservation;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class InvalidReservationMemberException extends RoomescapeException {
    public InvalidReservationMemberException() {
        super("해당 예약의 예약자가 아닙니다.", HttpStatus.BAD_REQUEST);
    }
}
