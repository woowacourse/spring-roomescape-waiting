package roomescape.exception;

import roomescape.common.BusinessException;

public class PaymentException extends BusinessException {

    public PaymentException(String message) {
        super(message);
    }

    public static class AmountMismatch extends PaymentException {

        public AmountMismatch(long expected, long actual) {
            super("결제 금액이 일치하지 않습니다. 저장된 금액=" + expected + ", 요청 금액=" + actual);
        }
    }
}
