package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class PaymentNotFoundException extends BusinessException {

    public PaymentNotFoundException() {
        super(ErrorType.PAYMENT_NOT_FOUND);
    }
}
