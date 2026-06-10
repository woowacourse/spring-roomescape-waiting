package roomescape.theme.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorCode;

public enum ThemeErrorCode implements ErrorCode {
    THEME_DUPLICATE(HttpStatus.CONFLICT, "이미 등록된 테마입니다."),
    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "테마를 찾을 수 없습니다."),
    THEME_INVALID_NAME(HttpStatus.BAD_REQUEST, "테마 이름은 공백일 수 없고 20자 이내여야 합니다."),
    THEME_INVALID_DESCRIPTION(HttpStatus.BAD_REQUEST, "테마 설명은 3자 이상이어야 합니다."),
    THEME_INVALID_URL(HttpStatus.BAD_REQUEST, "테마 이미지 URL은 비어 있을 수 없으며 올바른 URL 형식이어야 합니다."),
    RESERVATION_EXIST_ON_THEME(HttpStatus.CONFLICT, "예약이 있는 테마는 삭제할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ThemeErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getErrorMessage() {
        return message;
    }

    @Override
    public String getErrorName() {
        return this.name();
    }
}
