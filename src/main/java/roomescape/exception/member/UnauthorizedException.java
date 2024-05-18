package roomescape.exception.member;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomeescapeException;

public class UnauthorizedException extends RoomeescapeException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
