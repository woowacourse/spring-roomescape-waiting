package roomescape.exception.reservationtime;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class ReservationTimeAlreadyExistsException extends RoomescapeException {
    public ReservationTimeAlreadyExistsException() {
        super(HttpStatus.BAD_REQUEST, "이미 있는 시간은 추가할 수 없습니다.");
    }
}
