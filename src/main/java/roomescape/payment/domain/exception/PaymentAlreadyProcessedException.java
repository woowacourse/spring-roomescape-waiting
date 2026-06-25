package roomescape.payment.domain.exception;

import roomescape.common.exception.BusinessException;

public class PaymentAlreadyProcessedException extends BusinessException {

    public PaymentAlreadyProcessedException() {
        super(PaymentErrorType.ALREADY_PROCESSED);
    }
}
