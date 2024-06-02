package roomescape.exception.wait;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class NotFoundWaitException extends CustomException {
    public NotFoundWaitException() {
        super("존재하지 않는 예약입니다.", HttpStatus.NOT_FOUND);
    }
}
