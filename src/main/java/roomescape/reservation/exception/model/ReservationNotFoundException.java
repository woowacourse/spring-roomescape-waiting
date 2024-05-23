package roomescape.reservation.exception.model;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.model.RoomEscapeException;

public class ReservationNotFoundException extends RoomEscapeException {

    private static final String RESERVATION_NOT_EXIST_MESSAGE = "해당하는 예약이 존재하지 않습니다.";

    public ReservationNotFoundException() {
        super(RESERVATION_NOT_EXIST_MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
