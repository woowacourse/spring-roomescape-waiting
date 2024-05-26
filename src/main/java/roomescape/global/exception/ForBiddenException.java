package roomescape.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForBiddenException extends RuntimeException {

    public ForBiddenException() {
        super("허가되지않은 접근입니다.");
    }
}
