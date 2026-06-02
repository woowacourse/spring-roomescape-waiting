package roomescape.waiting.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorCode;

public enum WaitingErrorCode implements ErrorCode {
    WAITING_DUPLICATE(HttpStatus.CONFLICT, "이미 대기가 등록되어 있습니다."),
    WAITING_PAST_TIME(HttpStatus.UNPROCESSABLE_ENTITY, "과거 대기는 선택할 수 없습니다."),
    IMMEDIATE_RESERVATION_AVAILABLE(HttpStatus.UNPROCESSABLE_ENTITY, "즉시 예약이 가능하므로, 대기 등록이 불가능합니다."),
    CANNOT_WAITLIST_CONFIRMED_SLOT(HttpStatus.UNPROCESSABLE_ENTITY, "본인이 예약 확정한 슬롯에는 대기 등록이 불가능합니다."),
    INVALID_WAITING_NUMBER(HttpStatus.BAD_REQUEST, "대기 순서는 필수입니다."),
    WAITING_NOT_FOUND(HttpStatus.NOT_FOUND, "대기가 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    WaitingErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getErrorMessage() {
        return message;
    }

    @Override
    public String getErrorName() {
        return this.name();
    }
}
