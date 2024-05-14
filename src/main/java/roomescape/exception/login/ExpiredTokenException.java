package roomescape.exception.login;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class ExpiredTokenException extends RoomescapeException {
    public ExpiredTokenException() {
        super("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED);
    }
}
