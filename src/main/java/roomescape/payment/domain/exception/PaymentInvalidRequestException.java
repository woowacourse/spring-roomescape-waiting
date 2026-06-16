package roomescape.payment.domain.exception;

import roomescape.global.exception.InvalidRequestException;

public class PaymentInvalidRequestException extends InvalidRequestException {

    public PaymentInvalidRequestException(String message) {
        super(message);
    }
}
