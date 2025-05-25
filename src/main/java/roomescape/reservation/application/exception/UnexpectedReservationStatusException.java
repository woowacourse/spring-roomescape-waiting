package roomescape.reservation.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomEscapeException;
import roomescape.reservation.domain.ReservationStatus;

public class UnexpectedReservationStatusException extends RoomEscapeException {
    public UnexpectedReservationStatusException(ReservationStatus status) {
        super(HttpStatus.BAD_REQUEST, status.getValue() + " 상태의 예약이 아닙니다.");
    }
}
