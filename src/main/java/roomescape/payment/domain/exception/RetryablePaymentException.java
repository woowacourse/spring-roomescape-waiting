package roomescape.payment.domain.exception;

import roomescape.common.exception.BusinessException;

public class RetryablePaymentException extends BusinessException {

    public RetryablePaymentException() {
        super(PaymentErrorType.GATEWAY_RETRYABLE);
    }
}
