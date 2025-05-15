package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ExistedReservationException extends CustomException {

    private static final String MESSAGE = "예약이 존재합니다.";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public ExistedReservationException() {
        super(MESSAGE, STATUS);
    }
}
