package roomescape.payment.domain.exception;

import roomescape.global.exception.InvalidRequestException;

public class PaymentRejectedException extends InvalidRequestException {

    public PaymentRejectedException(String message) {
        super(message);
    }
}
