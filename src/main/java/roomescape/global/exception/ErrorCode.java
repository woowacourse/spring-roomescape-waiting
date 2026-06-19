package roomescape.global.exception;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum ErrorCode {
    INVALID_REQUEST(BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    INVALID_DATE_FORMAT(BAD_REQUEST, "날짜 형식이 잘못되었습니다. (yyyy-MM-dd)"),
    FORBIDDEN_RESERVATION_ACCESS(FORBIDDEN, "본인의 예약 정보만 접근 가능합니다."),
    PAST_RESERVATION(BAD_REQUEST, "지난 시간에는 예약할 수 없습니다."),
    RESERVATION_NOT_FOUND(NOT_FOUND, "예약을 찾을 수 없습니다."),
    RESERVATION_TIME_NOT_FOUND(NOT_FOUND, "예약 시간을 찾을 수 없습니다."),
    THEME_NOT_FOUND(NOT_FOUND, "테마를 찾을 수 없습니다."),
    RESERVATION_ALREADY_EXISTS(CONFLICT, "이미 예약이 존재합니다."),
    CANNOT_DELETE_RESERVED_TIME(CONFLICT, "이미 예약이 존재하는 시간대이므로 삭제할 수 없습니다."),
    CANNOT_DELETE_RESERVED_THEME(CONFLICT, "이미 예약이 존재하는 테마이므로 삭제할 수 없습니다."),
    DUPLICATED_RESERVATION_WAITING(CONFLICT, "이미 해당 예약의 대기가 존재합니다."),
    RESERVATION_WAITING_NOT_FOUND(NOT_FOUND, "예약 대기 정보를 찾을 수 없습니다."),
    FORBIDDEN_RESERVATION_WAITING_ACCESS(FORBIDDEN, "본인의 예약 대기 정보만 접근 가능합니다."),
    CANNOT_CANCEL_PAST_RESERVATION_WAITING(BAD_REQUEST, "이미 지난 시간의 예약 대기를 취소할 수 없습니다."),
    CANNOT_WAIT_WITHOUT_RESERVATION(BAD_REQUEST, "예약이 마감된 경우에만 대기 신청이 가능합니다."),
    PAYMENT_AMOUNT_MISMATCH(BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
