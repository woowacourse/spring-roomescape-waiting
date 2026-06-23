package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class CardPaymentRejectedException extends BusinessException {

    public CardPaymentRejectedException() {
        super(ErrorType.PAYMENT_CARD_REJECTED);
    }
}
