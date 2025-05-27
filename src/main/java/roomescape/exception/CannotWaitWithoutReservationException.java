package roomescape.exception;

import org.springframework.http.HttpStatus;

public class CannotWaitWithoutReservationException extends CustomException {

    private static final String MESSAGE = "예약이 존재하지 않아 대기할 수 없습니다.";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public CannotWaitWithoutReservationException() {
        super(MESSAGE, STATUS);
    }
}
