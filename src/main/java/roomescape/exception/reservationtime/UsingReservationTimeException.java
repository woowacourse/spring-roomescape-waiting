package roomescape.exception.reservationtime;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class UsingReservationTimeException extends RoomescapeException {
    public UsingReservationTimeException() {
        super(HttpStatus.BAD_REQUEST, "예약 되어있는 시간은 삭제할 수 없습니다.");
    }
}
