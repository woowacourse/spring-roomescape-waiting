package roomescape.payment.infra.toss;

import roomescape.payment.application.exception.PaymentErrorCode;
import roomescape.payment.application.exception.PaymentException;

public class OutboundRateLimitException extends PaymentException {

    public OutboundRateLimitException() {
        super(PaymentErrorCode.OUTBOUND_RATE_LIMIT_EXCEEDED);
    }
}
