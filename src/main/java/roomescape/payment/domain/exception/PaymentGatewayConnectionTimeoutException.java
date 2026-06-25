package roomescape.payment.domain.exception;

import roomescape.common.exception.BusinessException;

public class PaymentGatewayConnectionTimeoutException extends BusinessException {

    public PaymentGatewayConnectionTimeoutException() {
        super(PaymentErrorType.GATEWAY_CONNECTION_TIMEOUT);
    }
}
