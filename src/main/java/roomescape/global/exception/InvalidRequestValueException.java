package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestValueException extends BusinessException {

    public InvalidRequestValueException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    public InvalidRequestValueException() {
        this("값이 유효하지 않습니다.");
    }
}
