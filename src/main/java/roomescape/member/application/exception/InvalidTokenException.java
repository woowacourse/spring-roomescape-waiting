package roomescape.member.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomEscapeException;

public class InvalidTokenException extends RoomEscapeException {

    public InvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
