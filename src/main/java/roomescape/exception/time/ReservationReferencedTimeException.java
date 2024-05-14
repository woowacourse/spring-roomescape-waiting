package roomescape.exception.time;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class ReservationReferencedTimeException extends CustomException {
    public ReservationReferencedTimeException() {
        super("예약이 존재하는 시간입니다.", HttpStatus.BAD_REQUEST);
    }
}
