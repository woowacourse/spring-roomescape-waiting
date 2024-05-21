package roomescape.exception.login;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class UnauthorizedEmailException extends RoomescapeException {
    public UnauthorizedEmailException() {
        super("이메일이 틀립니다.", HttpStatus.UNAUTHORIZED);
    }
}
