package roomescape.payment.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorType;

public enum PaymentErrorType implements ErrorType {
    AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT400_001", "결제 금액이 주문 금액과 일치하지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "PAYMENT400_002", "올바르지 않은 결제 승인 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT404_001", "결제 주문을 찾을 수 없습니다."),
    SESSION_EXPIRED(HttpStatus.GONE, "PAYMENT410_001", "결제 세션이 만료되었습니다. 다시 결제해 주세요."),
    ALREADY_PROCESSED(HttpStatus.CONFLICT, "PAYMENT409_001", "이미 처리된 결제입니다."),
    DUPLICATED_ORDER(HttpStatus.CONFLICT, "PAYMENT409_002", "이미 사용된 주문 번호입니다."),
    OWNER_MISMATCH(HttpStatus.FORBIDDEN, "PAYMENT403_001", "본인의 결제 주문만 처리할 수 있습니다."),
    CARD_REJECTED(HttpStatus.UNPROCESSABLE_ENTITY, "PAYMENT422_001", "카드 결제가 거절되었습니다. 결제 수단을 확인해 주세요."),
    GATEWAY_CONFIGURATION(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT500_001", "결제 설정 오류가 발생했습니다."),
    GATEWAY_RETRYABLE(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT503_001",
            "결제 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해 주세요."),
    GATEWAY_ERROR(HttpStatus.BAD_GATEWAY, "PAYMENT502_001", "결제 승인 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String errorMessage;

    PaymentErrorType(HttpStatus httpStatus, String errorCode, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
