package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestFormatException extends BusinessException {

    public InvalidRequestFormatException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public InvalidRequestFormatException() {
        super(HttpStatus.BAD_REQUEST, "요청 본문 형식이 유효하지 않습니다.");
    }
}
