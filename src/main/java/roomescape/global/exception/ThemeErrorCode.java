package roomescape.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ThemeErrorCode implements ErrorCode {

    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "테마를 찾을 수 없습니다."),
    THEME_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "테마 이름을 입력해 주세요."),
    THEME_DESCRIPTION_REQUIRED(HttpStatus.BAD_REQUEST, "테마 설명을 입력해 주세요."),
    THEME_THUMBNAIL_REQUIRED(HttpStatus.BAD_REQUEST, "테마 썸네일을 입력해 주세요."),
    THEME_IN_USE(HttpStatus.BAD_REQUEST, "예약이 연결된 테마는 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
