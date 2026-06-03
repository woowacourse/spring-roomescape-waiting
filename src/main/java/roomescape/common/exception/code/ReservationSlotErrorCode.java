package roomescape.common.exception.code;

import org.springframework.http.HttpStatus;

public enum ReservationSlotErrorCode implements ErrorCode {
    PAST_DATETIME("지나간 날짜/시간은 선택할 수 없습니다.", HttpStatus.UNPROCESSABLE_ENTITY);

    private final String message;
    private final HttpStatus httpStatus;

    ReservationSlotErrorCode(String message, HttpStatus httpStatus) {
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
