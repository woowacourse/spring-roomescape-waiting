package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class PaymentOwnerMismatchException extends BusinessException {

    public PaymentOwnerMismatchException() {
        super(ErrorType.PAYMENT_OWNER_MISMATCH);
    }
}
