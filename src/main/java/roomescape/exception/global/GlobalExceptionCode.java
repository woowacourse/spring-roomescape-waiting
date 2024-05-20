package roomescape.exception.global;

import org.springframework.http.HttpStatus;
import roomescape.exception.model.ExceptionCode;

public enum GlobalExceptionCode implements ExceptionCode {

    METHOD_ARGUMENT_TYPE_INVALID(HttpStatus.BAD_REQUEST, "타입이 일치하지 않습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버에서 에러가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    GlobalExceptionCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
