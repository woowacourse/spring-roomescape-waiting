package roomescape.reservation.exception;

import roomescape.global.exception.InvalidRequestValueException;

public class InvalidReservationDateValueException extends InvalidRequestValueException {

    public InvalidReservationDateValueException() {
        super("예약 날짜가 유효하지 않습니다.");
    }
}
