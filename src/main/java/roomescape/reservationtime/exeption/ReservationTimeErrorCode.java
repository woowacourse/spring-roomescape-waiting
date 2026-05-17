package roomescape.reservationtime.exeption;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorPolicy;

import static org.springframework.http.HttpStatus.*;

public enum ReservationTimeErrorCode implements ErrorPolicy {
    INVALID_RESERVATION_TIME_ID("예약 시간 id는 비어 있을 수 없습니다.", BAD_REQUEST),
    INVALID_RESERVATION_TIME("예약 시간은 비어 있을 수 없습니다.", BAD_REQUEST),
    RESERVATION_TIME_ALREADY_HAS_ID("이미 id가 존재하는 예약 시간입니다.", CONFLICT),
    RESERVATION_TIME_ALREADY_EXISTS("이미 존재하는 예약 시간입니다.", CONFLICT),
    RESERVATION_TIME_NOT_FOUND("존재하지 않는 예약 시간입니다.", NOT_FOUND),
    RESERVATION_TIME_HAS_RESERVATION("예약이 있는 시간은 삭제할 수 없습니다.", CONFLICT);


    private final String code;
    private final String message;
    private final HttpStatus status;

    ReservationTimeErrorCode(String message, HttpStatus status) {
        this.code = name();
        this.message = message;
        this.status = status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }
}
