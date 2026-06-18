package roomescape.payment.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorCode;

public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 정보가 존재하지 않습니다."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 결제입니다. 잠시 후 다시 확인해주세요."),
    PAYMENT_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "결제 요청 정보가 올바르지 않습니다."),
    PAYMENT_DUPLICATED_ORDER_ID(HttpStatus.CONFLICT, "이미 사용된 주문번호입니다."),
    PAYMENT_SESSION_EXPIRED(HttpStatus.BAD_REQUEST, "결제 시간이 만료되었습니다. 다시 시도해주세요."),
    PAYMENT_UNAUTHORIZED_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "결제 처리 중 오류가 발생했습니다."),
    PAYMENT_REJECT_CARD(HttpStatus.PAYMENT_REQUIRED, "한도 초과 또는 잔액 부족으로 결제가 거절되었습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 결제 정보입니다."),
    PAYMENT_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "결제사 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    PAYMENT_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "결제 서버에 연결하지 못했습니다. 잠시 후 다시 시도해주세요."),
    PAYMENT_RESULT_UNKNOWN(HttpStatus.GATEWAY_TIMEOUT, "결제 결과를 확인하지 못했습니다. 결제 내역에서 상태를 확인하거나 다시 시도해주세요."),
    PAYMENT_FAILED(HttpStatus.BAD_GATEWAY, "결제에 실패했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String message;

    PaymentErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getErrorMessage() {
        return message;
    }

    @Override
    public String getErrorName() {
        return this.name();
    }
}
