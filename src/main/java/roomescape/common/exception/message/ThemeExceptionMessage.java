package roomescape.common.exception.message;

public enum ThemeExceptionMessage {
    DUPLICATE_THEME("이미 테마가 존재합니다."),
    RESERVED_THEME("이미 예약된 테마는 삭제할 수 없습니다.");

    private final String message;

    ThemeExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
