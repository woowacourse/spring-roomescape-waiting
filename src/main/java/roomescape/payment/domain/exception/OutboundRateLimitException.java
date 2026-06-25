package roomescape.payment.domain.exception;

import roomescape.common.exception.BusinessException;

public class OutboundRateLimitException extends BusinessException {

    public OutboundRateLimitException() {
        super(PaymentErrorType.OUTBOUND_RATE_LIMITED);
    }
}
