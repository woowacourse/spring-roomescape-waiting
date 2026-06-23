package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class RetryablePaymentException extends BusinessException {

    public RetryablePaymentException() {
        super(ErrorType.PAYMENT_GATEWAY_RETRYABLE);
    }
}
