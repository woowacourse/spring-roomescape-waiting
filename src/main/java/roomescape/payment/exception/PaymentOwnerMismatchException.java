package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;

public class PaymentOwnerMismatchException extends BusinessException {

    public PaymentOwnerMismatchException() {
        super(PaymentErrorType.OWNER_MISMATCH);
    }
}
