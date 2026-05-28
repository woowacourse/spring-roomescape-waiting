package roomescape.common.exception.code;

import org.springframework.http.HttpStatus;

public enum ReservationWaitingErrorCode implements ErrorCode {
    RESERVATION_WAITING_NOT_FOUND("존재하지 않는 예약 대기입니다.", HttpStatus.NOT_FOUND),
    RESERVATION_NOT_FOUND("존재하지 않는 예약에 대기를 신청할 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE("동일한 사용자 이름, 예약 날짜, 시간, 테마에 이미 예약 대기가 존재합니다.", HttpStatus.CONFLICT),
    PAST_DATETIME("지나간 날짜·시간에 대한 예약 생성은 불가능합니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    ;

    private final String message;
    private final HttpStatus httpStatus;

    ReservationWaitingErrorCode(String message, HttpStatus httpStatus) {
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
