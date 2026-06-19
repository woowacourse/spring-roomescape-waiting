package roomescape.payment;

import roomescape.domain.exception.NotFoundException;

public class PaymentOrderNotFoundException extends NotFoundException {

    public PaymentOrderNotFoundException(String message) {
        super(message);
    }
}
