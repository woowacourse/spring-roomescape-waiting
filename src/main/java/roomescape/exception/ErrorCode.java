package roomescape.exception;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum ErrorCode {
    // --- BAD_REQUEST 400 ---
    INVALID_REQUEST(BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    INVALID_NAME_FORMAT(BAD_REQUEST, "이름은 필수입니다."),
    INVALID_DATE_FORMAT(BAD_REQUEST, "날짜 형식이 잘못되었습니다. (yyyy-MM-dd)"),
    INVALID_RESERVATION_WAITING(BAD_REQUEST, "같은 예약 일정이 아닙니다."),
    CANNOT_RESERVE_PAST_DATETIME(BAD_REQUEST, "지난 시간에는 예약할 수 없습니다."),
    CANNOT_DELETE_PAST_RESERVATION(BAD_REQUEST, "지난 시간 예약을 취소할 수 없습니다."),
    RESERVATION_NOT_EXISTS(BAD_REQUEST, "예약이 존재하지 않습니다."),
    CANNOT_CANCEL_PAST_RESERVATION_WAITING(BAD_REQUEST, "이미 지난 시간의 예약 대기를 취소할 수 없습니다."),

    // --- FORBIDDEN 403 ---
    CANNOT_MODIFY_OTHER_RESERVATION(FORBIDDEN, "다른 사람 예약을 변경할 수 없습니다."),
    CANNOT_DELETE_OTHER_RESERVATION(FORBIDDEN, "다른 사람 예약을 취소할 수 없습니다."),
    FORBIDDEN_RESERVATION_WAITING_ACCESS(FORBIDDEN, "본인의 예약 대기 정보만 접근 가능합니다."),

    // --- NOT_FOUND 404 ---
    RESERVATION_NOT_FOUND(NOT_FOUND, "예약을 찾을 수 없습니다."),
    RESERVATION_TIME_NOT_FOUND(NOT_FOUND, "예약 시간을 찾을 수 없습니다."),
    THEME_NOT_FOUND(NOT_FOUND, "테마를 찾을 수 없습니다."),
    RESERVATION_WAITING_NOT_FOUND(NOT_FOUND, "예약 대기 정보를 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(NOT_FOUND, "결제 주문을 찾을 수 없습니다."),

    // --- CONFLICT 409 ---
    RESERVATION_ALREADY_EXISTS(CONFLICT, "이미 예약이 존재합니다."),
    CANNOT_DELETE_RESERVED_TIME(CONFLICT, "이미 예약이 존재하는 시간대이므로 삭제할 수 없습니다."),
    CANNOT_DELETE_RESERVED_THEME(CONFLICT, "이미 예약이 존재하는 테마이므로 삭제할 수 없습니다."),
    DUPLICATED_RESERVATION(CONFLICT, "본인 예약에 대기를 신청할 수 없습니다."),
    DUPLICATED_RESERVATION_WAITING(CONFLICT, "이미 해당 예약의 대기가 존재합니다."),
    PAYMENT_CONFIRM_FAILED(CONFLICT, "결제 승인이 완료되지 않았습니다."),
    PAYMENT_CONFIRM_UNKNOWN(CONFLICT, "결제 승인 결과를 확인할 수 없습니다. 내 예약에서 상태를 확인하거나 다시 시도해 주세요."),

    PAYMENT_GATEWAY_UNAVAILABLE(SERVICE_UNAVAILABLE, "결제 승인 서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요."),
    OUTBOUND_RATE_LIMIT_EXCEEDED(TOO_MANY_REQUESTS, "외부 결제 승인 호출량이 많습니다. 잠시 후 다시 시도해 주세요."),
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
