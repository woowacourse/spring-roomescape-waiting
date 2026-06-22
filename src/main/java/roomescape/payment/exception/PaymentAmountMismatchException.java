package roomescape.payment.exception;

import org.springframework.http.HttpStatus;

public class PaymentAmountMismatchException extends PaymentFailureException {

    public PaymentAmountMismatchException(Long expected, Long actual) {
        super(HttpStatus.BAD_REQUEST, "AMOUNT_MISMATCH",
                "결제 금액이 일치하지 않습니다. 저장된 금액=" + expected + ", 요청 금액=" + actual);
    }
}
