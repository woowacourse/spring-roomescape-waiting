package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class PaymentSessionExpiredException extends BusinessException {

    public PaymentSessionExpiredException() {
        super(ErrorType.PAYMENT_SESSION_EXPIRED);
    }
}
