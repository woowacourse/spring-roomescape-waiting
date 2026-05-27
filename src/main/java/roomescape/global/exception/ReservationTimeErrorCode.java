package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public enum ReservationTimeErrorCode implements ErrorCode {

    RESERVATION_TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 시간을 찾을 수 없습니다."),
    RESERVATION_TIME_START_AT_REQUIRED(HttpStatus.BAD_REQUEST, "예약 시간을 입력해 주세요."),
    RESERVATION_TIME_IN_USE(HttpStatus.BAD_REQUEST, "예약이 연결된 예약 시간은 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ReservationTimeErrorCode(
            HttpStatus status,
            String message
    ) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
