package roomescape.exception;

import org.springframework.http.HttpStatus;

public class PastDateTimeReservationException extends CustomException {

    private static final String MESSAGE = "지나간 날짜와 시간에 대한 예약 생성은 불가능합니다.";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public PastDateTimeReservationException() {
        super(MESSAGE, STATUS);
    }
}
