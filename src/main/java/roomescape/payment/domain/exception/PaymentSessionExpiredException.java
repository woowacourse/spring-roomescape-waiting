package roomescape.payment.domain.exception;

import roomescape.common.exception.BusinessException;

public class PaymentSessionExpiredException extends BusinessException {

    public PaymentSessionExpiredException() {
        super(PaymentErrorType.SESSION_EXPIRED);
    }
}
