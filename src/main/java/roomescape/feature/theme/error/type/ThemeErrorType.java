package roomescape.feature.theme.error.type;

import org.springframework.http.HttpStatus;
import roomescape.global.error.type.ErrorType;

public enum ThemeErrorType implements ErrorType {
    ALREADY_EXIST_THEME(HttpStatus.CONFLICT, "이미 등록된 테마입니다."),
    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "테마를 찾을 수 없습니다."),
    INVALID_NAME(HttpStatus.BAD_REQUEST, "테마 이름은 필수이며 255자 이하여야 합니다."),
    INVALID_DESCRIPTION(HttpStatus.BAD_REQUEST, "테마 설명은 필수이며 255자 이하여야 합니다."),
    INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "테마 이미지 URL은 필수이며 2000자 이하의 올바른 URL 형식이어야 합니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ThemeErrorType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return httpStatus;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public String code() {
        return name();
    }
}
