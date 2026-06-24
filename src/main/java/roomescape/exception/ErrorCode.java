package roomescape.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    //400 - BAD_REQUEST
    INVALID_RESERVATION_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 입력값 입니다."),
    MISSING_NAME(HttpStatus.BAD_REQUEST, "이름은 필수 입력 값입니다."),
    PAYMENT_SECRET_KEY_NOT_CONFIGURED(HttpStatus.BAD_REQUEST, "결제 시크릿 키가 설정되어있지 않습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "주문 금액과 결제 금액이 일치하지 않습니다."),

    //401 - UNAUTHORIZED
    UNAUTHORIZED_NAME(HttpStatus.UNAUTHORIZED, "해당 예약을 삭제할 권한이 없습니다."),

    //404 - NOT_FOUND
    TIME_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 time id를 찾을 수 없습니다."),
    THEME_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 theme id를 찾을 수 없습니다."),
    RESERVATION_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 reservation id를 찾을 수 없습니다."),
    WAITING_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 waiting id를 찾을 수 없습니다."),
    PAYMENT_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "결제할 주문을 찾을 수 없습니다."),

    //409 - CONFLICT
    DUPLICATE_RESERVATION(HttpStatus.CONFLICT, "이미 선택된 예약입니다."),
    TIME_DELETE_NOT_ALLOWED(HttpStatus.CONFLICT, "예약이 존재하는 시간을 삭제할 수 없습니다."),
    DUPLICATE_RESERVATION_NAME(HttpStatus.CONFLICT, "해당 이름의 예약이 이미 존재합니다."),
    DUPLICATE_WAITING_NAME(HttpStatus.CONFLICT, "해당 이름의 예약 대기가 이미 존재합니다."),
    DUPLICATE_TIME(HttpStatus.CONFLICT, "이미 등록된 시작 시간입니다."),

    //422 - UNPROCESSABLE CONTENT
    RESERVATION_TIME_PASSED(HttpStatus.UNPROCESSABLE_ENTITY, "이미 지난 날짜의 예약은 생성할 수 없습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY, "해당 예약은 점유되어있지 않습니다."),
    WAITING_NOT_AVAILABLE(HttpStatus.UNPROCESSABLE_ENTITY, "예약이 이미 등록되어있습니다.");

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
