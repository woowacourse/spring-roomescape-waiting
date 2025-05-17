package roomescape.exception.member;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomEscapeException;

public class InvalidTokenException extends RoomEscapeException {

    public InvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
