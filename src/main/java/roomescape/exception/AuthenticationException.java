package roomescape.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends RoomescapeException {

    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
