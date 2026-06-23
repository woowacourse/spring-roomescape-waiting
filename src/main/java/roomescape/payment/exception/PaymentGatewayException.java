package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class PaymentGatewayException extends BusinessException {

    public PaymentGatewayException() {
        super(ErrorType.PAYMENT_GATEWAY_ERROR);
    }
}
