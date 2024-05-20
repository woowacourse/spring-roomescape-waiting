package roomescape.reservationtime.exception;

import org.springframework.http.HttpStatus;
import roomescape.exception.model.ExceptionCode;

public enum TimeExceptionCode implements ExceptionCode {

    FOUND_TIME_IS_NULL_EXCEPTION(HttpStatus.BAD_REQUEST, "존재하는 시간이 없습니다."),
    TIME_IS_OUT_OF_OPERATING_TIME(HttpStatus.BAD_REQUEST, "운영 시간 외의 예약 시간 요청입니다."),

    DUPLICATE_TIME_EXCEPTION(HttpStatus.CONFLICT, "이미 존재하는 예약 시간입니다."),
    EXIST_RESERVATION_AT_CHOOSE_TIME(HttpStatus.CONFLICT, "삭제를 요청한 시간에 예약이 존재합니다.");

    private final HttpStatus httpStatus;
    private final String message;

    TimeExceptionCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
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
