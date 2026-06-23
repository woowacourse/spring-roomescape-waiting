package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class PaymentGatewayConfigurationException extends BusinessException {

    public PaymentGatewayConfigurationException() {
        super(ErrorType.PAYMENT_GATEWAY_CONFIGURATION);
    }
}
