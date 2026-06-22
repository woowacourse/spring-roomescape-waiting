package roomescape.exception.code;

import org.springframework.http.HttpStatus;
import roomescape.exception.ErrorCode;

public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다."),

    ALREADY_PROCESSED_PAYMENT(HttpStatus.BAD_REQUEST, "이미 처리된 결제입니다. 결제 내역을 확인해 주세요."),
    DUPLICATED_ORDER_ID(HttpStatus.BAD_REQUEST, "이미 사용된 주문 번호입니다."),
    NOT_FOUND_PAYMENT_SESSION(HttpStatus.BAD_REQUEST, "결제 시간이 만료되어 주문 정보를 찾을 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "결제 키 설정 오류입니다. 관리자에게 문의하세요."),
    INVALID_API_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "결제 API 키 설정 오류입니다. 관리자에게 문의하세요."),
    REJECT_CARD_PAYMENT(HttpStatus.BAD_REQUEST, "카드 결제가 거절되었습니다. 카드사에 문의해 주세요."),
    NOT_FOUND_PAYMENT(HttpStatus.NOT_FOUND, "존재하지 않는 결제 정보입니다."),
    FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING(HttpStatus.INTERNAL_SERVER_ERROR, "결제사 내부 오류입니다. 잠시 후 다시 시도해 주세요."),
    PAYMENT_GATEWAY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제 처리 중 오류가 발생했습니다."),
    PAYMENT_CONNECT_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "결제사에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요."),
    PAYMENT_READ_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "결제사 응답 대기 중 시간이 초과되었습니다. 결제 결과를 확인할 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    PaymentErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
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
