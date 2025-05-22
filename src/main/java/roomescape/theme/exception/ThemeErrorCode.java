package roomescape.theme.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorCode;

@AllArgsConstructor
public enum ThemeErrorCode implements ErrorCode {
    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "테마를 찾을 수 없습니다."),
    USING_THEME(HttpStatus.BAD_REQUEST, "예약에 사용 중인 테마입니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
