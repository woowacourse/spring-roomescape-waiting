package roomescape.exception;

import org.springframework.http.HttpStatus;

public class PastDateTimeException extends CustomException {

    private static final String MESSAGE = "지나간 날짜와 시간에 대한 예약/대기 생성은 불가능합니다.";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public PastDateTimeException() {
        super(MESSAGE, STATUS);
    }
}
