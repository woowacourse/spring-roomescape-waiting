package roomescape.service.exception;

public enum ErrorCode {

    INVALID_INPUT("입력값이 올바르지 않습니다."),
    INTERNAL_ERROR("일시적인 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED("지원하지 않는 요청 방식입니다."),
    RESOURCE_NOT_FOUND("요청한 리소스를 찾을 수 없습니다."),

    // Reservation
    RESERVATION_NOT_FOUND("존재하지 않는 예약입니다."),
    DUPLICATE_RESERVATION("이미 예약된 시간입니다."),
    RESERVATION_NOT_ALLOWED_WITH_WAITING("대기가 존재하는 시간에 예약할 수 없습니다."),

    // ReservationTime
    RESERVATION_TIME_NOT_FOUND("존재하지 않는 예약 시간입니다."),
    RESERVATION_TIME_IN_USE("예약이 존재하는 시간입니다."),

    // Theme
    THEME_NOT_FOUND("존재하지 않는 테마입니다."),

    // Waiting
    DUPLICATE_WAITING("이미 대기 중인 시간입니다."),
    INVALID_WAITING_RANK("대기 순번은 1 이상이어야 합니다."),
    WAITING_WITHOUT_RESERVATION("예약 가능한 시간에는 대기를 신청할 수 없습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
