package roomescape.exception.login;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class InvalidTokenException extends RoomescapeException {
    public InvalidTokenException() {
        super("토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
    }
}
