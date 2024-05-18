package roomescape.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super("[ERROR] 인증되지 않은 사용자입니다.");
    }

    public AuthenticationException(String message) {
        super("[ERROR] " + message);
    }
}
