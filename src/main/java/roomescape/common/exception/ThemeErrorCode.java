package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public enum ThemeErrorCode implements ErrorCode {
    INVALID_THEME_NAME_LENGTH("테마 이름 길이는 1자 ~ 50자 사이여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_THUMBNAIL_URL("유효하지 않은 이미지 주소입니다. URL은 https로 시작해야 합니다.", HttpStatus.BAD_REQUEST),
    THEME_NOT_FOUND("존재하지 않는 테마입니다. 입력을 확인해 주세요.", HttpStatus.NOT_FOUND),
    THEME_IN_USE("테마를 사용하는 예약이 존재합니다. 관련 예약을 지우고 요청해 주세요", HttpStatus.CONFLICT);

    private final String message;
    private final HttpStatus httpStatus;

    ThemeErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}