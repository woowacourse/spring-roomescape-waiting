package roomescape.payment.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.BusinessException;

public class PaymentAmountMismatchException extends BusinessException {

    public PaymentAmountMismatchException(Long expected, Long actual) {
        super(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다. 저장된 금액=" + expected + ", 요청 금액=" + actual);
    }
}
