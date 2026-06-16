package roomescape.payment.domain.exception;

import roomescape.global.exception.ConflictException;

public class PaymentAlreadyProcessedException extends ConflictException {

    public PaymentAlreadyProcessedException(String message) {
        super(message);
    }
}
