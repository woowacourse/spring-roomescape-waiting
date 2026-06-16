package roomescape.payment.domain.exception;

import roomescape.global.exception.InfrastructureException;

public class PaymentKeyConfigurationException extends InfrastructureException {

    public PaymentKeyConfigurationException(String message) {
        super(message);
    }
}
