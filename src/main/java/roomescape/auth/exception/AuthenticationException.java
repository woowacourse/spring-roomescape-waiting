package roomescape.auth.exception;

import org.springframework.http.HttpStatus;

public abstract class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
