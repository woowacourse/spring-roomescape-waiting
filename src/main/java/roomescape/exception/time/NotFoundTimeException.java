package roomescape.exception.time;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class NotFoundTimeException extends CustomException {
    public NotFoundTimeException() {
        super("존재하지 않는 시간입니다.", HttpStatus.NOT_FOUND);
    }
}
