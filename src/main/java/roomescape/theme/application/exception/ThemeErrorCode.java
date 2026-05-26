package roomescape.theme.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.ErrorCode;

public enum ThemeErrorCode implements ErrorCode {
    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 테마 입니다."),
    DUPLICATE_THEME(HttpStatus.CONFLICT, "이름과 설명이 같은 테마가 이미 존재합니다."),
    THEME_DELETE_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "예약이 존재하는 테마는 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ThemeErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String message() {
        return message;
    }
}
