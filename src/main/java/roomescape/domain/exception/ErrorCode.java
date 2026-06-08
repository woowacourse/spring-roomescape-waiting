package roomescape.domain.exception;

public enum ErrorCode {

    INPUT_FORMAT_ERROR("요청 형식이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다."),
    RESERVATION_SLOT_IN_PAST("예약 가능한 시간이 아닙니다."),
    RESERVATION_SLOT_NOT_FOUND("예약 가능한 슬롯을 찾을 수 없습니다."),
    RESERVATION_ALREADY_EXISTS("이미 예약된 시간입니다."),
    RESERVATION_SAME_SLOT("동일한 슬롯으로는 수정할 수 없습니다."),
    RESERVATION_NOT_FOUND("예약을 찾을 수 없습니다."),
    RESERVATION_NOT_OWNER("본인 예약만 취소할 수 있습니다."),
    RESERVATION_TIME_NOT_FOUND("예약 시간을 찾을 수 없습니다."),
    RESERVATION_TIME_IN_USE("예약에 사용 중인 시간입니다."),
    THEME_NOT_FOUND("테마를 찾을 수 없습니다."),
    THEME_IN_USE("예약에 사용 중인 테마입니다."),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS("이미 존재하는 사용자입니다."),
    UNAUTHORIZED("로그인이 필요합니다."),
    FORBIDDEN("권한이 없습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
