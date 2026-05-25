package roomescape.reservation.exception;

import roomescape.global.exception.InvalidRequestValueException;

public class InvalidReservationRequestValueException extends InvalidRequestValueException {

    public InvalidReservationRequestValueException() {
        super("예약 요청 값이 유효하지 않습니다.");
    }
}
