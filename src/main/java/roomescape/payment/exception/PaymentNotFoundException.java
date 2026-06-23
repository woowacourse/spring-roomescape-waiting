package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;

public class PaymentNotFoundException extends BusinessException {

    public PaymentNotFoundException() {
        super(PaymentErrorType.NOT_FOUND);
    }
}
