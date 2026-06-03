package roomescape.exception.code;

import org.springframework.http.HttpStatus;
import roomescape.exception.ErrorCode;

public enum ReservationTimeErrorCode implements ErrorCode {

    RESERVATION_TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약 시간입니다."),
    RESERVATION_TIME_HAS_RESERVATION(HttpStatus.CONFLICT, "해당 예약 시간에 예약이 존재합니다."),
    RESERVATION_TIME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 시간입니다."),
    INVALID_RESERVATION_TIME_RANGE(HttpStatus.UNPROCESSABLE_ENTITY, "영업 시간이 아닙니다."),
    INVALID_RESERVATION_TIME_UNIT(HttpStatus.BAD_REQUEST, "잘못된 예약 시간 단위입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    ReservationTimeErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
