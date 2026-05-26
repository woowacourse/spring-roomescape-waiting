package roomescape.common.exception.code;

import org.springframework.http.HttpStatus;

public enum ReservationTimeErrorCode implements ErrorCode {
    NOT_FOUND("존재하지 않는 예약 시간입니다.", HttpStatus.NOT_FOUND),
    RESERVATION_TIME_CANNOT_DELETE("예약된 시간은 삭제할 수 없습니다.", HttpStatus.CONFLICT),
    ;

    private final String message;
    private final HttpStatus httpStatus;

    ReservationTimeErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
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
