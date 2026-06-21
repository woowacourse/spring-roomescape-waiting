package roomescape.payment.application.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.ErrorCode;

public enum PaymentErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 대기 주문을 찾을 수 없습니다."),
    AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다."),
    ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 결제입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "만료되었거나 올바르지 않은 결제 요청입니다."),
    INVALID_API_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "결제 서비스 인증 설정에 오류가 있습니다."),
    CARD_REJECTED(HttpStatus.PAYMENT_REQUIRED, "카드 결제가 거절되었습니다. 다른 결제 수단을 이용해 주세요."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 건을 찾을 수 없습니다."),
    RETRYABLE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "결제 서비스의 일시적인 오류입니다. 잠시 후 다시 시도해 주세요."),
    UNKNOWN_GATEWAY_ERROR(HttpStatus.BAD_GATEWAY, "결제 승인 중 알 수 없는 오류가 발생했습니다."),
    PAYMENT_CANCELED(HttpStatus.BAD_REQUEST, "사용자가 결제를 취소했습니다.");

    private final HttpStatus status;
    private final String message;

    PaymentErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String message() {
        return message;
    }
}
