package roomescape.reservation.exception;

import roomescape.global.exception.InvalidRequestFormatException;

public class InvalidReservationNameException extends InvalidRequestFormatException {

    public InvalidReservationNameException() {
        super("예약자 이름 영문자로만 구성된 한 자 이상의 문자열이여야 합니다.");
    }
}
