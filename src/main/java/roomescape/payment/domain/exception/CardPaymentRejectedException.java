package roomescape.payment.domain.exception;

import roomescape.common.exception.BusinessException;

public class CardPaymentRejectedException extends BusinessException {

    public CardPaymentRejectedException() {
        super(PaymentErrorType.CARD_REJECTED);
    }
}
