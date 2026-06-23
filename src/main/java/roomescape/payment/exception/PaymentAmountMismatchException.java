package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class PaymentAmountMismatchException extends BusinessException {

    public PaymentAmountMismatchException() {
        super(ErrorType.PAYMENT_AMOUNT_MISMATCH);
    }
}
