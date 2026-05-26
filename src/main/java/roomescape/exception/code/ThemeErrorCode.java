package roomescape.exception.code;

import org.springframework.http.HttpStatus;
import roomescape.exception.ErrorCode;

public enum ThemeErrorCode implements ErrorCode {

    THEME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 테마입니다."),
    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 테마입니다."),
    THEME_HAS_RESERVATION(HttpStatus.CONFLICT, "해당 테마를 지닌 예약이 존재합니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    ThemeErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
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
