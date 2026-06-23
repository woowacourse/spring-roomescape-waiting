package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;

public class InvalidPaymentRequestException extends BusinessException {

    public InvalidPaymentRequestException() {
        super(PaymentErrorType.INVALID_REQUEST);
    }
}
