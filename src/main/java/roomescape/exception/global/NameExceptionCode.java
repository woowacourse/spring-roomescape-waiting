package roomescape.exception.global;

import org.springframework.http.HttpStatus;
import roomescape.exception.model.ExceptionCode;

public enum NameExceptionCode implements ExceptionCode {

    NAME_LENGTH_IS_OVER_MAX_COUNT(HttpStatus.BAD_REQUEST, "이름의 길이가 최대 길이를 넘었습니다."),
    NAME_IS_NULL_OR_BLANK_EXCEPTION(HttpStatus.BAD_REQUEST, "이름을 입력해주세요."),
    ILLEGAL_NAME_FORM_EXCEPTION(HttpStatus.BAD_REQUEST, "특수문자가 포함된 이름으로 예약을 시도하였습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    NameExceptionCode(HttpStatus httpStatus, String message) {
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
