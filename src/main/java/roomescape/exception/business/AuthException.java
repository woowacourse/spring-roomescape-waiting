package roomescape.exception.business;

import org.springframework.http.HttpStatus;

public class AuthException extends BusinessException {

    public AuthException() {
        super(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
}