package roomescape.reservation.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomEscapeException;

public class ReservationAlreadyExistsException extends RoomEscapeException {
    public ReservationAlreadyExistsException() {
        super(HttpStatus.BAD_REQUEST, "이미 예약이 존재합니다.");
    }
}
