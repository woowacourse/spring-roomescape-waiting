package roomescape.exception.business;

import org.springframework.http.HttpStatus;

public class PastTimeReservationException extends BusinessException {

    public PastTimeReservationException() {
        super(HttpStatus.BAD_REQUEST, "이미 지난 시간으로 변경할 수 없습니다.");
    }
}
