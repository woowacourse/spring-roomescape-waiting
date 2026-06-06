package roomescape.reservation.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.ErrorCode;

public enum ReservationErrorCode implements ErrorCode {
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."),
    DUPLICATE_RESERVATION(HttpStatus.CONFLICT, "이미 해당 날짜와 시간에 예약이 존재합니다."),
    DUPLICATE_WAITING(HttpStatus.CONFLICT, "이미 해당 날짜와 시간에 대기 신청이 존재합니다."),
    PAST_RESERVATION_TIME(HttpStatus.UNPROCESSABLE_ENTITY, "현재 시간보다 이전 시간으로 예약을 할 수 없습니다."),
    PAST_RESERVATION_MODIFICATION(HttpStatus.UNPROCESSABLE_ENTITY, "지난 예약은 변경하거나 취소할 수 없습니다."),
    FORBIDDEN_RESERVATION_ACCESS(HttpStatus.FORBIDDEN, "본인의 예약만 변경하거나 취소할 수 있습니다.");

    private final HttpStatus status;
    private final String message;

    ReservationErrorCode(HttpStatus status, String message) {
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
