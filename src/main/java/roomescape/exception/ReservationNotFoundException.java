package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ReservationNotFoundException extends CustomException {

    private static final String MESSAGE = "예약이 존재하지 않습니다.";
    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public ReservationNotFoundException() {
        super(MESSAGE, STATUS);
    }
}
