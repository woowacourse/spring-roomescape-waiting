package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;

public class PaymentGatewayException extends BusinessException {

    public PaymentGatewayException() {
        super(PaymentErrorType.GATEWAY_ERROR);
    }
}
