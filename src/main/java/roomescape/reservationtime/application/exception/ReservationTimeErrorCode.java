package roomescape.reservationtime.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.ErrorCode;

public enum ReservationTimeErrorCode implements ErrorCode {
    TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 시간 입니다."),
    DUPLICATE_TIME(HttpStatus.CONFLICT, "시간 %s이(가) 이미 존재합니다."),
    TIME_DELETE_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "예약이 존재하는 시간대는 삭제할 수 없습니다");

    private final HttpStatus status;
    private final String message;

    ReservationTimeErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String message() {
        return message;
    }
}
