package roomescape.exception.business;

import org.springframework.http.HttpStatus;

public class DuplicateReservationException extends BusinessException {

    public DuplicateReservationException() {
        super(HttpStatus.CONFLICT, "이미 예약된 시간입니다.");
    }
}
