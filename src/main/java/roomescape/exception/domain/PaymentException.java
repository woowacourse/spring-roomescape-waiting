package roomescape.exception.domain;

import roomescape.exception.RoomescapeException;
import roomescape.exception.code.PaymentErrorCode;

public class PaymentException extends RoomescapeException {

    public PaymentException(PaymentErrorCode paymentErrorCode) {
        super(paymentErrorCode);
    }
}
