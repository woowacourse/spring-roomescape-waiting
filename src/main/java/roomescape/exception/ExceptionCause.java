package roomescape.exception;

public enum ExceptionCause {

    RESERVATION_INVALID_FOR_PAST("현 시점 이후의 날짜와 시간을 선택해주세요."),
    RESERVATION_ALREADY_BOOKED("해당 날짜와 테마로 이미 예약된 내역이 존재합니다."),
    RESERVATION_NOTFOUND("예약을 찾을 수 없습니다."),
    RESERVATION_DATE_EMPTY_INPUT("예약 날짜는 반드시 입력해야 합니다"),

    MEMBER_NOTFOUND("회원을 찾을 수 없습니다."),
    MEMBER_DUPLICATE_EMAIL("이미 가입한 이메일입니다."),
    MEMBER_NAME_INVALID_INPUT("사용자의 이름은 1글자 ~ 5글자로 이루어져야 합니다."),
    MEMBER_PASSWORD_INVALID_INPUT("비밀번호는 10자 이상이어야 합니다."),
    MEMBER_EMAIL_INVALID_INPUT("이메일 형식을 지켜야합니다."),
    MEMBER_EMPTY_INPUT("회원 정보는 반드시 입력해야 합니다."),

    RESERVATION_TIME_EMPTY_INPUT("예약 시간을 반드시 입력해야 합니다."),
    RESERVATION_TIME_NOTFOUND("예약 시간을 찾을 수 없습니다."),
    RESERVATION_TIME_DUPLICATE("이미 동일한 예약 시간이 존재합니다."),
    RESERVATION_TIME_EXIST("해당 시간에 예약 기록이 존재합니다. 예약을 먼저 삭제해 주세요."),

    THEME_NOTFOUND("예약 시간을 찾을 수 없습니다."),
    THEME_EMPTY_INPUT("테마를 반드시 입력해야 합니다."),
    THEME_NAME_INVALID_INPUT("테마의 이름은 1글자 이상으로 이루어져야 합니다."),
    THEME_NAME_DUPLICATE("이미 동일한 이름의 테마가 존재합니다."),
    THEME_DESCRIPTION_INVALID_INPUT("테마 설명이 없습니다."),
    THEME_THUMBNAIL_INVALID_INPUT("테마 이미지가 없습니다."),
    THEME_EXIST("해당 테마에 예약 기록이 존재합니다. 예약을 먼저 삭제해 주세요."),

    UNAUTHORIZED_PAGE_ACCESS("접근 권한이 없습니다."),
    UNAUTHORIZED_LOGIN_ACCESS("로그인 정보를 다시 확인해 주세요."),

    INPUT_INVALID("값이 잘못되었습니다"),

    JWT_TOKEN_EMPTY("로그인이 필요합니다."),
    JWT_TOKEN_INVALID("JWT 토큰이 올바르지 않습니다."),
    JWT_TOKEN_EXPIRED("JWT 토큰이 만료되었습니다.");

    private final String message;

    ExceptionCause(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
