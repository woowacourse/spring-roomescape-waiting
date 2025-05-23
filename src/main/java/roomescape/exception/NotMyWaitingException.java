package roomescape.exception;

import org.springframework.http.HttpStatus;

public class NotMyWaitingException extends CustomException {

    private static final String MESSAGE = "자신의 대기가 아닙니다.";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public NotMyWaitingException() {
        super(MESSAGE, STATUS);
    }
}
