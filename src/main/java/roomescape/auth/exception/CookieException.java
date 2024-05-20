package roomescape.auth.exception;

import org.springframework.http.HttpStatus;
import roomescape.exception.model.ExceptionCode;

public enum CookieException implements ExceptionCode {

    COOKIE_IS_NULL_EXCEPTION(HttpStatus.UNAUTHORIZED, "쿠키가 비어있습니다."),
    COOKE_VALUE_IS_NOT_FOUND_EXCEPTION(HttpStatus.UNAUTHORIZED, "찾으려는 쿠키 값이 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    CookieException(HttpStatus httpStatus, String message) {
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
