package roomescape.exception;

public enum ErrorCode {
    // 테마
    THEME_NOT_EXIST("존재하지 않는 테마입니다."),
    THEME_NAME_TOO_LONG("테마 이름은 %d자를 넘길 수 없습니다."),
    RESERVED_THEME("해당 테마의 예약이 존재합니다."),

    // 예약
    RESERVATION_NOT_EXIST("존재하지 않는 예약입니다."),
    RESERVATION_DUPLICATED("중복된 예약입니다."),

    // 예약 날짜
    RESERVATION_DATE_PAST("과거 날짜로 예약할 수 없습니다."),
    RESERVATION_DATE_TOO_FAR_IN_FUTURE("%d일 전부터 예약할 수 있습니다."),

    // 예약 시간
    RESERVATION_TIME_NOT_EXIST("존재하지 않는 예약 시간입니다."),
    RESERVATION_TIME_ALREADY_EXIST("해당 예약 시간은 이미 존재합니다."),
    RESERVATION_TIME_INTERVAL_INVALID("예약 시간은 30분 간격으로만 생성할 수 있습니다."),
    RESERVED_RESERVATION_TIME("해당 예약 시간의 예약이 존재합니다."),
    START_TIME_INVALID("시작 시간은 %s~%s 만 가능합니다."),

    // 유저
    USER_NOT_EXIST("존재하지 않는 유저입니다."),
    USER_NAME_LENGTH_TOO_LONG("유저 이름은 %d자를 넘길 수 없습니다."),
    USER_NAME_CONTAINS_NUMBER("유저 이름에 숫자는 포함될 수 없습니다."),
    EMAIL_DUPLICATED("중복된 이메일입니다."),
    EMAIL_FORMAT_INVALID("이메일 형식이어야 합니다."),
    ;

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
