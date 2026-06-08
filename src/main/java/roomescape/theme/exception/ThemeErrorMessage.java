package roomescape.theme.exception;

import roomescape.global.ErrorMessage;

public enum ThemeErrorMessage implements ErrorMessage {

    THEME_NOT_FOUND("해당하는 ID(%s)의 테마가 존재하지 않습니다."),
    THEME_ALREADY_DELETED("이미 삭제된 테마입니다."),
    DUPLICATE_THEME("이미 존재하는 테마 이름입니다."),
    THEME_IN_USE("해당 테마에 예약이 존재하여 삭제할 수 없습니다."),
    ;

    private final String message;

    ThemeErrorMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
