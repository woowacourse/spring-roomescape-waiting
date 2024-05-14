package roomescape.theme.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.model.ExceptionCode;

public enum ThemeExceptionCode implements ExceptionCode {

    USING_THEME_RESERVATION_EXIST(HttpStatus.BAD_REQUEST, "삭제하려는 테마의 예약이 아직 존재합니다."),
    FOUND_THEME_IS_NULL_EXCEPTION(HttpStatus.BAD_REQUEST, "존재하는 테마가 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ThemeExceptionCode(HttpStatus httpStatus, String message) {
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
