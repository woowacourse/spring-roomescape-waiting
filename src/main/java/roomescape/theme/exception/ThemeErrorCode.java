package roomescape.theme.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorPolicy;

public enum ThemeErrorCode implements ErrorPolicy {
    INVALID_THEME_ID("테마 id는 비어 있을 수 없습니다.", BAD_REQUEST),
    INVALID_THEME_NAME("테마 이름은 비어 있을 수 없습니다.", BAD_REQUEST),
    INVALID_THEME_DESCRIPTION("테마 설명은 비어 있을 수 없습니다.", BAD_REQUEST),
    INVALID_THEME_THUMBNAIL("테마 썸네일은 비어 있을 수 없습니다.", BAD_REQUEST),
    INVALID_THEME("테마 정보는 비어 있을 수 없습니다.", BAD_REQUEST),
    THEME_ALREADY_HAS_ID("이미 id가 존재하는 테마입니다.", CONFLICT),
    THEME_NOT_FOUND("존재하지 않는 테마입니다.", NOT_FOUND),
    THEME_HAS_RESERVATION("예약이 있는 테마는 삭제할 수 없습니다.", CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ThemeErrorCode(String message, HttpStatus status) {
        this.code = name();
        this.message = message;
        this.status = status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }
}
