package roomescape.payment.domain.exception;

import roomescape.common.exception.BusinessException;

public class PaymentGatewayResponseTimeoutException extends BusinessException {

    public PaymentGatewayResponseTimeoutException() {
        super(PaymentErrorType.GATEWAY_RESPONSE_TIMEOUT);
    }
}
