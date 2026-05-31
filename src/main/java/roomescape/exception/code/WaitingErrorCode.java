package roomescape.exception.code;

import org.springframework.http.HttpStatus;
import roomescape.exception.ErrorCode;

public enum WaitingErrorCode implements ErrorCode {

    WAITING_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 대기입니다."),
    WAITING_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 대기입니다."),
    CANNOT_WAIT_OWN_RESERVATION(HttpStatus.CONFLICT, "본인의 예약에는 대기를 신청할 수 없습니다."),
    RESERVATION_REQUIRED_FOR_WAITING(HttpStatus.BAD_REQUEST, "예약이 없는 슬롯에는 대기를 생성할 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    WaitingErrorCode(HttpStatus httpStatus, String message) {
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
