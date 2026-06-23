package roomescape.reservation.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorType;

public enum ReservationErrorType implements ErrorType {
    PAST_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RESERVATION400_001", "지나간 날짜와 시간으로는 예약할 수 없습니다."),
    PAST_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RESERVATION400_002", "이미 지난 예약은 취소할 수 없습니다."),
    OWNER_MISMATCH(HttpStatus.BAD_REQUEST, "RESERVATION400_003", "본인 예약만 변경/취소할 수 있습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION404_001", "존재하지 않는 예약입니다."),
    ALREADY_EXISTS(HttpStatus.CONFLICT, "RESERVATION409_001", "이미 예약이 존재합니다.");

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String errorMessage;

    ReservationErrorType(HttpStatus httpStatus, String errorCode, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
