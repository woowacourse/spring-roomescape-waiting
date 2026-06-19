package roomescape.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    WAITING_LIST_NOT_REQUIRED(HttpStatus.UNPROCESSABLE_ENTITY, "해당 시간에 예약이 존재하지 않기 때문에 예약 대기 불가합니다."),
    ALREADY_ON_WAITING_LIST(HttpStatus.UNPROCESSABLE_ENTITY, "이미 해당 조건의 예약 대기 신청이 존재합니다."),
    TIME_ALREADY_RESERVED(HttpStatus.UNPROCESSABLE_ENTITY, "해당 시간대에 이미 예약이 존재합니다."),
    DATE_ALREADY_PASSED(HttpStatus.UNPROCESSABLE_ENTITY, "이미 지난 날짜입니다."),
    TIME_ALREADY_PASSED(HttpStatus.UNPROCESSABLE_ENTITY, "이미 지난 시간입니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "주문 금액과 결제 금액이 일치하지 않습니다."),

    USER_NAME_NOT_MATCHED(HttpStatus.FORBIDDEN, "예약자와 사용자 이름이 일치하지 않습니다."),

    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 테마 정보를 찾을 수 없습니다."),
    TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 예약 시간 정보를 찾을 수 없습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 예약 정보를 찾을 수 없습니다."),
    WAITING_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 예약 대기 정보를 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 결제 정보를 찾을 수 없습니다."),

    TIME_HAS_RESERVATION(HttpStatus.CONFLICT, "해당 시간대에 잔여 예약이 존재합니다."),
    TIME_HAS_WAITING_LIST(HttpStatus.CONFLICT, "해당 시간대에 잔여 예약 대기가 존재합니다."),
    THEME_HAS_RESERVATION(HttpStatus.CONFLICT, "해당 테마에 잔여 예약이 존재합니다."),
    THEME_HAS_WAITING_LIST(HttpStatus.CONFLICT, "해당 테마에 잔여 예약 대기가 존재합니다."),
    QUEUED_WAITING_LIST(HttpStatus.CONFLICT, "해당 시간대에 예약 대기가 존재합니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(final HttpStatus httpStatus, final String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
