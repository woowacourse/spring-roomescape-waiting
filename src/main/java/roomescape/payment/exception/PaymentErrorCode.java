package roomescape.payment.exception;

import roomescape.global.exception.ErrorCode;

public enum PaymentErrorCode implements ErrorCode {
    DUPLICATE_PAYMENT("이미 결제 정보가 존재합니다."),
    PAYMENT_NOT_FOUND("결제 정보를 찾을 수 없습니다."),
    RESERVATION_NOT_FOUND("예약 정보를 찾을 수 없습니다.");

    private final String message;

    PaymentErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
