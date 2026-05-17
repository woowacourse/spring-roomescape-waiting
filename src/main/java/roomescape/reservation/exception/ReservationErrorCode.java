package roomescape.reservation.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorPolicy;

import static org.springframework.http.HttpStatus.*;

public enum ReservationErrorCode implements ErrorPolicy {
    INVALID_RESERVATION_ID("예약 id는 비어 있을 수 없습니다.", BAD_REQUEST),
    INVALID_RESERVATION_GUEST_NAME("예약자 이름은 비어 있을 수 없습니다.", BAD_REQUEST),
    INVALID_RESERVATION_DATE("예약 날짜는 비어 있을 수 없습니다.", BAD_REQUEST),
    RESERVATION_ALREADY_HAS_ID("이미 식별자가 존재하는 예약입니다.", CONFLICT),
    RESERVATION_ALREADY_EXISTS("이미 존재하는 예약입니다.", CONFLICT),
    RESERVATION_NOT_FOUND("존재하지 않는 예약입니다.", NOT_FOUND),
    PAST_RESERVATION_NOT_ALLOWED("이미 지난 시간에는 예약할 수 없습니다.", UNPROCESSABLE_ENTITY),
    CANNOT_EDIT_ALREADY_STARTED_RESERVATION("이미 시작된 예약은 수정할 수 없습니다.", UNPROCESSABLE_ENTITY),
    CANNOT_EDIT_OTHER_GUEST_RESERVATION("본인의 예약만 수정할 수 있습니다.", FORBIDDEN);


    private final String code;
    private final String message;
    private final HttpStatus status;

    ReservationErrorCode(String message, HttpStatus status) {
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
