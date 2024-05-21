package roomescape.exception.reservation;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class InvalidDateTimeReservationException extends CustomException {
    public InvalidDateTimeReservationException() {
        super("지난간 날짜와 시간에 대한 예약입니다.", HttpStatus.BAD_REQUEST);
    }
}
