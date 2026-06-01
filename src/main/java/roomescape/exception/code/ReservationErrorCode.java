package roomescape.exception.code;

import org.springframework.http.HttpStatus;
import roomescape.exception.ErrorCode;

public enum ReservationErrorCode implements ErrorCode {

    RESERVATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 시간에는 이미 예약이 존재합니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."),
    PAST_DATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "지난 날짜는 선택할 수 없습니다."),
    RESERVATION_CANCEL_DEADLINE_PASSED(HttpStatus.UNPROCESSABLE_ENTITY, "예약 변경 및 취소 가능한 시간이 지났습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    ReservationErrorCode(HttpStatus httpStatus, String message) {
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
