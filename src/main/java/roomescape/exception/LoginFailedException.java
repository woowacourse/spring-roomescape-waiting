package roomescape.exception;

import org.springframework.http.HttpStatus;

public class LoginFailedException extends CustomException {

    private static final String MESSAGE = "이메일 또는 비밀번호를 확인해주세요.";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public LoginFailedException() {
        super(MESSAGE, STATUS);
    }
}
