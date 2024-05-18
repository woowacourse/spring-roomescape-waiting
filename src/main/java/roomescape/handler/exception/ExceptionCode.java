package roomescape.handler.exception;

public enum ExceptionCode {

    NOT_FOUND_RESERVATION("예약을 찾을 수 없습니다."),
    NOT_FOUND_RESERVATION_TIME("예약 시간을 찾을 수 없습니다."),
    NOT_FOUND_THEME("테마를 찾을 수 없습니다."),
    NOT_FOUND_USER("사용자를 찾을 수 없습니다."),
    TIME_IN_USE("이미 해당 시간에 예약이 존재하여 삭제할 수 없습니다."),
    PAST_TIME_SLOT_RESERVATION("이미 지나간 시점을 예약할 수 없습니다."),
    DUPLICATE_RESERVATION("동일한 시간에 중복 예약을 할 수 없습니다."),
    NO_AUTHENTICATION_INFO("권한 정보가 없는 토큰입니다."),
    BAD_REQUEST("잘못된 요청입니다."),
    NO_AUTHENTICATION_ACCESS("접근 권한이 없습니다.");

    private final String errorMessage;

    ExceptionCode(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
