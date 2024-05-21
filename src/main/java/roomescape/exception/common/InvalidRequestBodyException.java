package roomescape.exception.common;

import org.springframework.http.HttpStatus;

public class InvalidRequestBodyException extends RoomescapeException {
    public InvalidRequestBodyException() {
        super("요청 body에 유효하지 않은 필드가 존재합니다.", HttpStatus.BAD_REQUEST);
    }
}
