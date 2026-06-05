package roomescape.reservation.exception;

import roomescape.global.exception.ErrorCode;

public enum ReservationErrorCode implements ErrorCode {
    RESERVATION_NOT_FOUND("요청하신 예약을 찾을 수 없습니다. 예약 번호를 다시 확인해 주세요."),
    DUPLICATE_RESERVATION("이미 예약된 시간대입니다. 다른 날짜나 시간을 선택해 주세요."),
    INVALID_DATE("과거 날짜로는 예약할 수 없습니다. 오늘 이후의 유효한 날짜를 선택해 주세요."),
    INVALID_TIME("이미 지난 시간으로는 예약할 수 없습니다. 예약 가능한 미래 시간을 선택해 주세요."),
    INVALID_UPDATE_FORMAT("예약 날짜 또는 예약 시간 중 하나는 반드시 입력해야 합니다. 안내된 양식에 맞춰 다시 입력해 주세요."),
    AUTHORIZATION_FAIL("해당 작업에 대한 권한이 없습니다. 권한이 있는 계정으로 다시 로그인해 주세요."),
    MISSING_AUTH_HEADER("인증 정보가 만료되었거나 없습니다. 다시 로그인한 후 시도해 주세요."),
    ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME("해당 시간에 다른 테마의 예약 혹은 대기가 존재합니다.");

    private final String message;

    ReservationErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
