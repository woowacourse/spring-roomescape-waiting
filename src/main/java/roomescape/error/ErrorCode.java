package roomescape.error;

public enum ErrorCode {
    TIME_NOT_FOUND("예약 시간을 찾을 수 없습니다."),
    TIME_IN_USE("해당 시간에 예약이 존재하여 삭제할 수 없습니다."),
    RESERVATION_NOT_FOUND("예약을 찾을 수 없습니다."),
    DUPLICATE_RESERVATION("중복 예약은 불가합니다."),
    PAST_RESERVATION_CREATE("과거 날짜·시간은 예약이 불가합니다."),
    PAST_RESERVATION_UPDATE("이미 지난 예약은 변경할 수 없습니다."),
    PAST_RESERVATION_CANCEL("이미 지난 예약은 취소할 수 없습니다."),
    HOLIDAY_NOT_FOUND("휴일 정보를 찾을 수 없습니다."),
    THEME_NOT_FOUND("테마를 찾을 수 없습니다."),
    INVALID_FORMAT("요청 형식이 올바르지 않습니다."),
    INVALID_REQUEST("입력값이 유효하지 않습니다."),
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다."),
    RESERVATION_FORBIDDEN_REQUEST("예약에 대한 접근 권한이 없습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
