package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    METHOD_ARGUMENT_NOT_VALID(HttpStatus.BAD_REQUEST, "COMMON400_001", "유효하지 않은 요청필드입니다."),
    MISSING_PATH_VARIABLE(HttpStatus.BAD_REQUEST, "COMMON400_002", "경로 변수(PathVariable)가 누락됐습니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON400_003", "쿼리 스트링이 누락됐습니다."),
    HTTP_MESSAGE_NOT_READABLE(HttpStatus.BAD_REQUEST, "COMMON400_004", "올바른 입력값 형식이 아닙니다."),
    METHOD_ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "COMMON400_005", "올바른 쿼리 스트링 형식이 아닙니다."),
    CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "COMMON400_006", "유효하지 않은 쿼리 스트링 값입니다."),
    UNEXPECTED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500_001", "예기치 못한 예외가 발생했습니다."),

    PAST_RESERVATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RESERVATION400_001", "지나간 날짜와 시간으로는 예약할 수 없습니다."),
    PAST_RESERVATION_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RESERVATION400_002", "이미 지난 예약은 취소할 수 없습니다."),
    RESERVATION_OWNER_MISMATCH(HttpStatus.BAD_REQUEST, "RESERVATION400_003", "본인 예약만 변경/취소할 수 있습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION404_001", "존재하지 않는 예약입니다."),
    RESERVATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "RESERVATION409_001", "이미 예약이 존재합니다."),

    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT400_001", "결제 금액이 주문 금액과 일치하지 않습니다."),
    PAYMENT_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "PAYMENT400_002", "올바르지 않은 결제 승인 요청입니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT404_001", "결제 주문을 찾을 수 없습니다."),
    PAYMENT_SESSION_EXPIRED(HttpStatus.GONE, "PAYMENT410_001", "결제 세션이 만료되었습니다. 다시 결제해 주세요."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "PAYMENT409_001", "이미 처리된 결제입니다."),
    PAYMENT_DUPLICATED_ORDER(HttpStatus.CONFLICT, "PAYMENT409_002", "이미 사용된 주문 번호입니다."),
    PAYMENT_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "PAYMENT403_001", "본인의 결제 주문만 처리할 수 있습니다."),
    PAYMENT_CARD_REJECTED(HttpStatus.UNPROCESSABLE_ENTITY, "PAYMENT422_001", "카드 결제가 거절되었습니다. 결제 수단을 확인해 주세요."),
    PAYMENT_GATEWAY_CONFIGURATION(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT500_001", "결제 설정 오류가 발생했습니다."),
    PAYMENT_GATEWAY_RETRYABLE(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT503_001", "결제 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해 주세요."),
    PAYMENT_GATEWAY_ERROR(HttpStatus.BAD_GATEWAY, "PAYMENT502_001", "결제 승인 중 오류가 발생했습니다."),

    RESERVATION_TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_TIME404_001", "존재하지 않는 예약시간입니다."),
    RESERVATION_TIME_IN_USE(HttpStatus.CONFLICT, "RESERVATION_TIME409_001", "예약이 존재하는 예약시간은 삭제할 수 없습니다."),

    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "THEME404_001", "존재하지 않는 테마입니다."),
    THEME_IN_USE(HttpStatus.CONFLICT, "THEME409_001", "예약이 존재하는 테마는 삭제할 수 없습니다."),

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH401_001", "이메일 또는 비밀번호가 일치하지 않습니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH401_002", "로그인이 필요한 요청입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_004", "토큰이 만료되었습니다. 다시 로그인해 주세요."),

    INSUFFICIENT_ROLE(HttpStatus.FORBIDDEN, "AUTH403_001", "권한이 없습니다."),
    WRONG_STORE_ACCESS(HttpStatus.FORBIDDEN, "AUTH403_002", "다른 매장에 접근권한이 없습니다."),
    RESERVATION_WAIT_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_WAIT404_001", "존재하지 않는 예약 대기입니다."),

    PAST_RESERVATION_WAIT_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "RESERVATION_WAIT422_001",
            "예약 대기 시간이 이미 지난 시간입니다."),
    RESERVATION_WAIT_ALREADY_EXISTS(HttpStatus.CONFLICT, "RESERVATION_WAIT409_001", "이미 해당 슬롯에 대기 중입니다."),
    SELF_RESERVATION_WAIT_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "RESERVATION_WAIT422_002", "본인 예약에는 대기를 신청할 수 없습니다."),
    PENDING_RESERVATION_WAIT_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "RESERVATION_WAIT422_003",
            "결제가 완료되지 않은 예약에는 대기를 신청할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String errorMessage;

    ErrorType(HttpStatus httpStatus, String errorCode, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
