package roomescape.exception;

import org.springframework.http.HttpStatus;

public class DuplicatedEmailException extends CustomException {

    private static final String MESSAGE = "해당 이메일에 대한 회원이 존재합니다.";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public DuplicatedEmailException() {
        super(MESSAGE, STATUS);
    }
}
