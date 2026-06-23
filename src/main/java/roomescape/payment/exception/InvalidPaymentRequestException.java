package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class InvalidPaymentRequestException extends BusinessException {

    public InvalidPaymentRequestException() {
        super(ErrorType.PAYMENT_INVALID_REQUEST);
    }
}
