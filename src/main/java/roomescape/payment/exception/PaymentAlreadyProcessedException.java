package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class PaymentAlreadyProcessedException extends BusinessException {

    public PaymentAlreadyProcessedException() {
        super(ErrorType.PAYMENT_ALREADY_PROCESSED);
    }
}
