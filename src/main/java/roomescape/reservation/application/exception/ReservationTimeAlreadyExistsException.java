package roomescape.reservation.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomEscapeException;

public class ReservationTimeAlreadyExistsException extends RoomEscapeException {
    public ReservationTimeAlreadyExistsException() {
        super(HttpStatus.BAD_REQUEST, "이미 있는 시간은 추가할 수 없습니다.");
    }
}
