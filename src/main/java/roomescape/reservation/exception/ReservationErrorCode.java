package roomescape.reservation.exception;

public enum ReservationErrorCode {
    RESERVATION_NOT_FOUND("예약이 존재하지 않습니다."),
    DUPLICATE_RESERVATION("예약이 이미 존재합니다."),
    INVALID_DATE("예약 날짜가 유효하지 않습니다."),
    INVALID_FORMAT("예약 요청 형식이 유효하지 않습니다."),
    AUTHORIZATION_FAIL("접근 권한이 없습니다."),
    MISSING_AUTH_HEADER("인증 헤더가 존재하지 않습니다.");

    private final String message;

    ReservationErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
