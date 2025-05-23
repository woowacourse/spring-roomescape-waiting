package roomescape.exception;

import org.springframework.http.HttpStatus;

public class WaitingNotFoundException extends CustomException {

    private static final String MESSAGE = "대기가 존재하지 않습니다.";
    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public WaitingNotFoundException() {
        super(MESSAGE, STATUS);
    }
}
