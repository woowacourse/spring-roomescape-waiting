package roomescape.reservation.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorCode;

public enum ReservationErrorCode implements ErrorCode {
    RESERVATION_DUPLICATE(HttpStatus.CONFLICT, "같은 날짜, 같은 시간에 예약이 존재합니다."),
    RESERVATION_PAST_TIME(HttpStatus.UNPROCESSABLE_ENTITY, "과거 예약은 선택할 수 없습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약이 존재하지 않습니다."),
    INVALID_NAME(HttpStatus.BAD_REQUEST, "이름은 공백일 수 없고 20자 이내여야 합니다."),
    INVALID_DATE(HttpStatus.BAD_REQUEST, "예약 날짜는 필수입니다."),
    INVALID_TIME(HttpStatus.BAD_REQUEST, "예약 시간은 필수입니다."),
    INVALID_THEME(HttpStatus.BAD_REQUEST, "유효한 테마를 선택해주세요.");

    private final HttpStatus httpStatus;
    private final String message;

    ReservationErrorCode(HttpStatus httpStatus, String message) {
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
