package roomescape.payment.application.exception;

import roomescape.common.exception.UnauthorizedException;

public class PaymentUnauthorizedException extends UnauthorizedException {
    public PaymentUnauthorizedException(String message) {
        super(message);
    }
}
