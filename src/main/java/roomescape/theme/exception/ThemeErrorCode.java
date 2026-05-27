package roomescape.theme.exception;

public enum ThemeErrorCode {
    THEME_NOT_FOUND("테마가 존재하지 않습니다."),
    DUPLICATE_THEME("이미 존재하는 테마입니다."),
    THEME_IN_USE("예약된 테마는 삭제할 수 없습니다."),
    INVALID_FORMAT("테마 요청 형식이 유효하지 않습니다.");

    private final String message;

    ThemeErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
