package roomescape.theme.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorInformation;

@Getter
@AllArgsConstructor
public enum ThemeErrorInformation implements ErrorInformation {

    ID_IS_NULL(HttpStatus.BAD_REQUEST, "THEME_001", "테마 ID가 누락되었습니다."),
    NAME_IS_NULL(HttpStatus.BAD_REQUEST, "THEME_002", "테마 이름이 누락되었습니다."),
    DESCRIPTION_IS_NULL(HttpStatus.BAD_REQUEST, "THEME_003", "테마 설명이 누락되었습니다."),
    THUMBNAIL_URL_IS_NULL(HttpStatus.BAD_REQUEST, "THEME_004", "테마 썸네일 URL이 누락되었습니다."),

    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "THEME_005", "테마를 찾을 수 없습니다."),
    THEME_ALREADY_EXISTS(HttpStatus.CONFLICT, "THEME_006", "이미 등록된 테마입니다."),
    THEME_STATUS_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "THEME_007", "테마 활성화/비활성화 상태 변경에 실패했습니다."),
    INACTIVE_THEME_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "THEME_008", "해당 테마은 비활성화 되었습니다."),
    AMOUNT_IS_NULL(HttpStatus.BAD_REQUEST, "THEME_009", "테마 가격이 누락되었습니다."),
    AMOUNT_IS_NEGATIVE(HttpStatus.BAD_REQUEST, "THEME_010", "테마 가격은 음수일 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

}
