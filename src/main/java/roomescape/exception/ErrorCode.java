package roomescape.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 시스템 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,"지원하지 않는 HTTP 메서드입니다."),

    // 비즈니스 에러
    WAITING_LIST_NOT_REQUIRED(HttpStatus.UNPROCESSABLE_ENTITY, "해당 시간에 예약이 존재하지 않기 때문에 예약 대기 불가합니다."),
    ALREADY_ON_WAITING_LIST(HttpStatus.UNPROCESSABLE_ENTITY, "이미 해당 조건의 예약 대기 신청이 존재합니다."),
    ALREADY_RESERVED_BY_SELF(HttpStatus.UNPROCESSABLE_ENTITY, "본인이 이미 예약한 슬롯에는 대기 신청할 수 없습니다."),
    TIME_ALREADY_RESERVED(HttpStatus.UNPROCESSABLE_ENTITY, "해당 시간대에 이미 예약이 존재합니다."),
    DATE_ALREADY_PASSED(HttpStatus.UNPROCESSABLE_ENTITY, "이미 지난 날짜입니다."),
    TIME_ALREADY_PASSED(HttpStatus.UNPROCESSABLE_ENTITY, "이미 지난 시간입니다."),

    USER_NAME_NOT_MATCHED(HttpStatus.FORBIDDEN, "예약자와 사용자 이름이 일치하지 않습니다."),

    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 테마 정보를 찾을 수 없습니다."),
    TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 예약 시간 정보를 찾을 수 없습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 예약 정보를 찾을 수 없습니다."),
    WAITING_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 예약 대기 정보를 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문 정보를 찾을 수 없습니다."),

    TIME_HAS_RESERVATION(HttpStatus.CONFLICT, "해당 시간대에 잔여 예약이 존재합니다."),
    THEME_HAS_RESERVATION(HttpStatus.CONFLICT, "해당 테마에 잔여 예약이 존재합니다."),

    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다."),

    // 검증 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),

    AMOUNT_NULL(HttpStatus.BAD_REQUEST, "결제 금액은 비워둘 수 없습니다."),
    RESERVATION_ID_NULL(HttpStatus.BAD_REQUEST, "예약 ID는 비워둘 수 없습니다."),
    PERSON_NAME_NULL_OR_BLANK(HttpStatus.BAD_REQUEST, "예약자 이름은 비워둘 수 없습니다."),
    DATE_NULL(HttpStatus.BAD_REQUEST, "날짜는 비워둘 수 없습니다."),
    INVALID_DATE_TIME_FORMAT(HttpStatus.BAD_REQUEST, "날짜 또는 시간 형식이 올바르지 않습니다. (날짜 지정 형식: yyyy-mm-dd, 시간 지정 형식: hh:mm)"),

    TIME_ID_NULL(HttpStatus.BAD_REQUEST, "예약 시간 ID는 비워둘 수 없습니다."),
    START_TIME_NULL(HttpStatus.BAD_REQUEST, "시작 시간은 비워둘 수 없습니다."),
    END_TIME_NULL(HttpStatus.BAD_REQUEST, "종료 시간은 비워둘 수 없습니다."),

    THEME_ID_NULL(HttpStatus.BAD_REQUEST, "테마 ID는 비워둘 수 없습니다."),
    THEME_NAME_NULL_OR_BLANK(HttpStatus.BAD_REQUEST, "테마 이름은 비워둘 수 없습니다."),
    DESCRIPTION_NULL_OR_BLANK(HttpStatus.BAD_REQUEST, "테마 설명은 비워둘 수 없습니다."),
    DESCRIPTION_TOO_SHORT(HttpStatus.BAD_REQUEST, "테마 설명은 최소 5자 이상이어야 합니다."),
    THUMBNAIL_URL_NULL_OR_BLANK(HttpStatus.BAD_REQUEST, "테마 썸네일 URL은 비워둘 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(final HttpStatus httpStatus, final String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
