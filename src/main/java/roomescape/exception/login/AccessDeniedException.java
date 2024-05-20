package roomescape.exception.login;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class AccessDeniedException extends RoomescapeException {
    public AccessDeniedException() {
        super("권한이 없습니다.", HttpStatus.FORBIDDEN);
    }
}
