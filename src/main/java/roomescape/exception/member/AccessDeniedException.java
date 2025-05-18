package roomescape.exception.member;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomEscapeException;

public class AccessDeniedException extends RoomEscapeException {
    public AccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
