package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;

public class PaymentAmountMismatchException extends BusinessException {

    public PaymentAmountMismatchException() {
        super(PaymentErrorType.AMOUNT_MISMATCH);
    }
}
