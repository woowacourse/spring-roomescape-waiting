package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ReservationTimeNotFoundException extends CustomException {

    private static final String MESSAGE = "예약시간이 존재하지 않습니다.";
    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public ReservationTimeNotFoundException() {
        super(MESSAGE, STATUS);
    }
}
