package roomescape.common.exception.message;

public enum RequestExceptionMessage {
    INVALID_DATE("날짜를 입력해주세요"),
    INVALID_TIME("시간을 입력해주세요."),
    INVALID_NAME("이름을 입력해주세요."),
    INVALID_STRING("설명을 입력해주세요."),
    DATE_BEFORE_NOW("이미 지난 날짜로는 예약할 수 없습니다."),
    INVALID_TIME_ID("시간 아이디를 입력해주세요"),
    INVALID_THEME_ID("테마 아이디를 입력해주세요"),
    INVALID_MEMBER_ID("멤버 아이디를 입력해주세요");

    private final String message;

    RequestExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
