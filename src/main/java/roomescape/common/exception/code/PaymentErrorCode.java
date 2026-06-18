package roomescape.common.exception.code;

import org.springframework.http.HttpStatus;

public enum PaymentErrorCode implements ErrorCode {
    AMOUNT_MISMATCH("결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND("존재하지 않는 결제 주문입니다.", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus httpStatus;

    PaymentErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
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
