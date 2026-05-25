package roomescape.reservation.exception;

import roomescape.global.exception.InvalidRequestFormatException;

public class InvalidReservationRequestFormatException extends InvalidRequestFormatException {

    public InvalidReservationRequestFormatException() {
        super("예약 요청 형식이 유효하지 않습니다.");
    }
}
