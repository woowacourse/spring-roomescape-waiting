package roomescape.support.exception.errors;

import lombok.Getter;

@Getter
public enum ThemeErrors implements Errors {

    INVALID_THEME("테마는 필수입니다."),
    INVALID_THEME_NAME_LENGTH("테마 이름은 10자 이하여야 합니다."),
    THEME_NOT_EXIST("존재하지 않는 테마 입니다."),
    THEME_IN_USE("이미 예약이 존재하는 테마는 삭제할 수 없습니다."),
    ;

    private final String message;

    ThemeErrors(String message) {
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }
}
