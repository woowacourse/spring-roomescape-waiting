package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),

    // reservation
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약이 존재하지 않습니다."),
    RESERVATION_NOT_ALLOWED(HttpStatus.FORBIDDEN, "자신의 예약만 접근할 수 있습니다."),
    RESERVATION_ALREADY_CANCELLED(HttpStatus.CONFLICT, "이미 예약이 취소되었습니다."),
    RESERVATION_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 예약이 완료되었습니다."),
    RESERVATION_TIME_OUT(HttpStatus.UNPROCESSABLE_ENTITY, "이미 예약 시간이 지났습니다."),
    RESERVATION_NOT_ALLOWED_DATE(HttpStatus.UNPROCESSABLE_ENTITY, "과거 날짜 시간은 예약할 수 없습니다."),
    RESERVATION_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 해당 날짜, 시간, 테마에 예약이 존재합니다."),
    RESERVATION_ALREADY_EXIST_BY_USER_AND_SLOT(HttpStatus.CONFLICT,"이미 같은 시간에 예약 또는 대기를 신청했습니다."),

    // reservation status
    INVALID_PENDING_COMMAND(HttpStatus.UNPROCESSABLE_ENTITY, "대기 중인 예약은 취소나 확인만 가능합니다."),
    INVALID_CONFIRMED_COMMAND(HttpStatus.UNPROCESSABLE_ENTITY, "확인된 예약은 취소나 완료만 가능합니다."),
    INVALID_COMPLETED_COMMAND(HttpStatus.UNPROCESSABLE_ENTITY, "예약이 완료되었습니다."),
    ALREADY_CANCELLED_COMMAND(HttpStatus.UNPROCESSABLE_ENTITY, "이미 취소된 예약입니다."),
    INVALID_CANCELLED_COMMAND(HttpStatus.UNPROCESSABLE_ENTITY, "취소할 수 없는 예약입니다."),


    // time
    TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "시간이 존재하지 않습니다."),
    TIME_IS_REFERENCED(HttpStatus.UNPROCESSABLE_ENTITY, "예약에 해당하는 시간이 존재합니다."),

    // theme
    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "테마가 존재하지 않습니다."),
    THEME_IS_REFERENCED(HttpStatus.UNPROCESSABLE_ENTITY, "예약에 해당하는 시간이 존재합니다."),

    // theme slot
    THEME_SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 가능한 시간이 존재하지 않습니다."),

    // payment
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "결제 금액이 주문 금액과 일치하지 않습니다."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "이미 처리된 결제입니다."),
    PAYMENT_CARD_REJECTED(HttpStatus.FORBIDDEN, "카드 결제가 거절되었습니다."),
    PAYMENT_UNAUTHORIZED_KEY(HttpStatus.UNAUTHORIZED, "결제 인증 키가 유효하지 않습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    PAYMENT_TOSS_INTERNAL_ERROR(HttpStatus.BAD_GATEWAY, "결제 서버 내부 오류입니다. 잠시 후 재시도해 주세요."),
    PAYMENT_UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제 처리 중 알 수 없는 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
