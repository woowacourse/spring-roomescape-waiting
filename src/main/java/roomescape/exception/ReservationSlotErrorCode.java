package roomescape.exception;

import org.springframework.http.HttpStatus;

public enum ReservationSlotErrorCode implements ErrorCode {
    INVALID_RESERVATION_SLOT(HttpStatus.CONFLICT, "유효하지 않은 예약 슬롯입니다."),
    SLOT_PAST_TIME(HttpStatus.UNPROCESSABLE_ENTITY, "과거 날짜 및 시간은 선택할 수 없습니다.");
    private final HttpStatus httpStatus;

    private final String message;

    ReservationSlotErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getErrorMessage() {
        return message;
    }

    @Override
    public String getErrorName() {
        return this.name();
    }
}
