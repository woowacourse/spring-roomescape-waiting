package roomescape.payment.order;

import roomescape.service.exception.ResourceNotFoundException;

public class PaymentOrderNotFoundException extends ResourceNotFoundException {

    public PaymentOrderNotFoundException(String message) {
        super(message);
    }
}
