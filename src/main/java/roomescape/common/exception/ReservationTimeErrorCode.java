package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public enum ReservationTimeErrorCode implements ErrorCode{
    RESERVATION_TIME_NOT_FOUND("존재하지 않는 시간입니다. 입력을 확인해 주세요.", HttpStatus.NOT_FOUND),
    RESERVATION_TIME_IN_USE("시간을 사용하는 예약이 존재합니다. 관련 예약을 지우고 요청해 주세요.", HttpStatus.CONFLICT);

    private final String message;
    private final HttpStatus httpStatus;

    ReservationTimeErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
