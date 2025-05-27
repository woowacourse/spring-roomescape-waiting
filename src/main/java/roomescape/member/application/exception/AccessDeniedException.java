package roomescape.member.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomEscapeException;

public class AccessDeniedException extends RoomEscapeException {
    public AccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
