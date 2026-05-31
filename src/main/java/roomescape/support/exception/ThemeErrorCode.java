package roomescape.support.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ThemeErrorCode implements ErrorCode {
    INVALID_THEME(HttpStatus.BAD_REQUEST,
        "테마 엔티티 식별자 정보가 누락되었습니다.", "themeId 필드 포함 여부를 확인하십시오."),
    THEME_NOT_EXIST(HttpStatus.NOT_FOUND,
        "지정한 식별자에 해당하는 테마 엔티티를 찾을 수 없습니다.", "요청한 테마 ID의 유효성 및 DB 존재 여부를 확인하십시오."),
    THEME_IN_USE(HttpStatus.CONFLICT,
        "외래 키 제약 조건으로 인해 삭제가 불가능합니다. (해당 테마를 참조하는 예약 존재)", "해당 테마와 연관된 예약 엔티티들을 먼저 처리하십시오."),
    INVALID_THEME_NAME(HttpStatus.BAD_REQUEST,
        "테마 명칭 데이터가 유효하지 않습니다.", "name 필드의 유효성 제약 조건을 확인하십시오."),
    INVALID_THEME_CONTENT(HttpStatus.BAD_REQUEST,
        "테마 설명 데이터가 유효하지 않습니다.", "content 필드의 유효성 제약 조건을 확인하십시오."),
    INVALID_THEME_URL(HttpStatus.BAD_REQUEST,
        "테마 포스터 URL 데이터가 유효하지 않거나 형식이 올바르지 않습니다.", "url 필드의 형식 및 도메인 유효성을 확인하십시오."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String action;

    ThemeErrorCode(HttpStatus httpStatus, String message, String action) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.action = action;
    }

    @Override
    public String getCode() {
        return name();
    }
}
