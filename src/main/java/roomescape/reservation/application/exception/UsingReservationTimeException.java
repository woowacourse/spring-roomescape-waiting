package roomescape.reservation.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomEscapeException;

public class UsingReservationTimeException extends RoomEscapeException {
    public UsingReservationTimeException() {
        super(HttpStatus.BAD_REQUEST, "예약 되어있는 시간은 삭제할 수 없습니다.");
    }
}
