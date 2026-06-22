package roomescape.exception.domain;

import roomescape.exception.RoomescapeException;
import roomescape.exception.code.PaymentErrorCode;

public class PaymentException extends RoomescapeException {

    public PaymentException(PaymentErrorCode paymentErrorCode) {
        super(paymentErrorCode);
    }

    public PaymentException(PaymentErrorCode paymentErrorCode, Throwable cause) {
        super(paymentErrorCode, cause);
    }
}
