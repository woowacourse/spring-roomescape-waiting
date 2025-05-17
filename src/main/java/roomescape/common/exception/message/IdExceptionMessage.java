package roomescape.common.exception.message;

public enum IdExceptionMessage {
    INVALID_MEMBER_ID("해당 멤버 아이디는 존재하지 않습니다"),
    INVALID_TIME_ID("해당 시간 아이디는 존재하지 않습니다."),
    INVALID_THEME_ID("해당 테마 아이디는 존재하지 않습니다."),
    INVALID_RESERVATION_ID("해당 예약 아이디는 존재하지 않습니다");

    private final String message;

    IdExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
