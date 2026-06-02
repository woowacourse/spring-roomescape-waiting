package roomescape.reservationtime.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorCode;

public enum ReservationTimeErrorCode implements ErrorCode {

    RESERVATION_TIME_DUPLICATE(HttpStatus.CONFLICT, "이미 등록된 예약 시간입니다."),
    RESERVATION_TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 시간을 찾을 수 없습니다."),
    RESERVATION_TIME_INVALID_ID(HttpStatus.BAD_REQUEST, "예약 시간 ID는 1 이상의 숫자여야 합니다."),
    RESERVATION_EXIST_ON_TIME(HttpStatus.CONFLICT, "예약이 있는 시간은 삭제할 수 없습니다."),
    INVALID_TIME(HttpStatus.BAD_REQUEST, "예약 시간은 필수이며 정각만 선택할 수 있습니다.");
    private final HttpStatus httpStatus;
    private final String message;

    ReservationTimeErrorCode(HttpStatus httpStatus, String message) {
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
