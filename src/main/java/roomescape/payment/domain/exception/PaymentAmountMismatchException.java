package roomescape.payment.domain.exception;

import roomescape.global.exception.InvalidRequestException;

public class PaymentAmountMismatchException extends InvalidRequestException {

    public PaymentAmountMismatchException(String message) {
        super(message);
    }
}
