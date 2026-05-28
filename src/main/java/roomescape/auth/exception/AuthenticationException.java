package roomescape.auth.exception;

import roomescape.global.exception.BusinessException;

public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException() {
        super("인증에 실패하였습니다.");
    }
}
