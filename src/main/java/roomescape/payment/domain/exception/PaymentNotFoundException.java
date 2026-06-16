package roomescape.payment.domain.exception;

import roomescape.global.exception.NotFoundException;

public class PaymentNotFoundException extends NotFoundException {

    public PaymentNotFoundException(String message) {
        super(message);
    }
}
