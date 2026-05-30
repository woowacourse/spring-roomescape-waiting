package roomescape.reservation.exception;

public enum ReservationErrorCode {
    RESERVATION_NOT_FOUND("요청하신 예약을 찾을 수 없습니다. 예약 번호를 다시 확인해 주세요."),
    DUPLICATE_RESERVATION("이미 예약된 시간대입니다. 다른 날짜나 시간을 선택해 주세요."),
    INVALID_DATE("선택하신 날짜에 예약할 수 없습니다. 예약 가능한 다른 날짜를 선택해 주세요."),
    INVALID_FORMAT("입력 형식이 올바르지 않습니다. 안내된 양식에 맞춰 다시 입력해 주세요."),
    AUTHORIZATION_FAIL("해당 작업에 대한 권한이 없습니다. 권한이 있는 계정으로 다시 로그인해 주세요."),
    MISSING_AUTH_HEADER("인증 정보가 만료되었거나 없습니다. 다시 로그인한 후 시도해 주세요.");

    private final String message;

    ReservationErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
