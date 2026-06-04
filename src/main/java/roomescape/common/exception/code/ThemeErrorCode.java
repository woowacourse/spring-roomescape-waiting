package roomescape.common.exception.code;

import org.springframework.http.HttpStatus;

public enum ThemeErrorCode implements ErrorCode {
    NOT_FOUND("존재하지 않는 테마입니다.", HttpStatus.NOT_FOUND),
    DUPLICATE("이미 존재하는 테마입니다.", HttpStatus.CONFLICT),
    THEME_CANNOT_DELETE("예약된 테마는 삭제할 수 없습니다.", HttpStatus.CONFLICT),
    ;

    private final String message;
    private final HttpStatus httpStatus;

    ThemeErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
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
