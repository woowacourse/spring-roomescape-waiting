package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ExistedWaitingException extends CustomException {

    private static final String MESSAGE = "대기가 존재합니다.";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public ExistedWaitingException() {
        super(MESSAGE, STATUS);
    }
}
