package roomescape.payment.domain.exception;

import roomescape.common.exception.BusinessException;

public class PaymentGatewayConfigurationException extends BusinessException {

    public PaymentGatewayConfigurationException() {
        super(PaymentErrorType.GATEWAY_CONFIGURATION);
    }
}
