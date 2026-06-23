package roomescape.reservationwait.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorType;

public enum ReservationWaitErrorType implements ErrorType {
    NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_WAIT404_001", "존재하지 않는 예약 대기입니다."),
    PAST_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "RESERVATION_WAIT422_001", "예약 대기 시간이 이미 지난 시간입니다."),
    ALREADY_EXISTS(HttpStatus.CONFLICT, "RESERVATION_WAIT409_001", "이미 해당 슬롯에 대기 중입니다."),
    SELF_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "RESERVATION_WAIT422_002", "본인 예약에는 대기를 신청할 수 없습니다."),
    PENDING_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "RESERVATION_WAIT422_003",
            "결제가 완료되지 않은 예약에는 대기를 신청할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String errorMessage;

    ReservationWaitErrorType(HttpStatus httpStatus, String errorCode, String errorMessage) {
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
